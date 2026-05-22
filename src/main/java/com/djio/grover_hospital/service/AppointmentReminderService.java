package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.repository.BookingRepository;
import com.djio.grover_hospital.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Sends 24-hour appointment reminders.
 * <p>
 * "Tomorrow" is computed in Africa/Lagos so the day boundary matches the
 * hospital's local time regardless of server timezone.
 * <p>
 * Only CONFIRMED bookings are reminded — a PENDING booking hasn't had its
 * time agreed yet, so a "see you tomorrow" reminder would be premature.
 * (If you'd rather remind PENDING too, add a second pass for that status.)
 * <p>
 * Idempotency: each booking gets reminderSentForDate stamped after a send,
 * and the finder only returns rows where it's null, so re-running the cron
 * the same day won't double-send. A reschedule clears the stamp, re-arming it.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderService {

    private static final ZoneId HOSPITAL_ZONE = ZoneId.of("Africa/Lagos");

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Transactional
    public int sendRemindersForTomorrow() {
        LocalDate tomorrow = LocalDate.now(HOSPITAL_ZONE).plusDays(1);

        List<Booking> due = bookingRepository
                .findByPreferredDateAndStatusAndReminderSentForDateIsNull(
                        tomorrow, BookingStatus.CONFIRMED
                );

        if (due.isEmpty()) {
            log.debug("Appointment reminder sweep: no confirmed bookings for {}", tomorrow);
            return 0;
        }

        log.info("Appointment reminder sweep: {] confirmed bookin(s) for {}", due.size(), tomorrow);

        int sent = 0;
        for (Booking booking : due) {
            try {
                notificationService.notifyAppointmentReminderToPatient(booking);
                booking.setReminderSentForDate(tomorrow);
                bookingRepository.save(booking);
                sent++;
            } catch (Exception e) {
                log.error("Failed to send reminder for booking {}: {}", booking.getId(), e.getMessage(), e);
            }
        }

        log.info("Appointment reminder sweep complete: {}/{} reminders dispatched", sent, due.size());
        return sent;
    }
}
