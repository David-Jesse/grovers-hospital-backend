package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.RescheduleBookingRequest;
import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.repository.BookingRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Patient-initiated booking reschedule.
 * <p>
 * Rules:
 * - Patient can only reschedule their OWN bookings.
 * - Only PENDING or CONFIRMED bookings can be rescheduled
 * (COMPLETED/CANCELLED are terminal).
 * - On reschedule the status resets to PENDING — the previously agreed
 * time is void and the admin must re-confirm the new date.
 * - The new date must differ from the current one.
 * - Fires the existing booking-status-update notification so the patient
 * and the in-portal bell reflect the change, and the hospital is alerted.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingRescheduleService {

    private static final String RESOURCE_TYPE = "BOOKING";

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public Booking reschedule(Long bookingId, RescheduleBookingRequest request, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Ownership check -  patient can only touch their own bookings
        if (booking.getPatient() == null || !booking.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You can only reschedule your own bookings");
        }

        // Status guard - only now terminal bookings
        BookingStatus current = booking.getStatus();
        if (current == BookingStatus.COMPLETED || current == BookingStatus.CANCELLED) {
            throw new BadRequestException("A " + current.name().toLowerCase() + " booking cannot be rescheduled");
        }

        // No-op guard - new date must differ
        if (booking.getPreferredDate() != null
                && booking.getPreferredDate().equals(request.getNewPreferredDate())
        ) {
            throw new BadRequestException("The new data is the same as the current booking date");
        }

        var oldDate = booking.getPreferredDate();

        // Apply the reschedule
        booking.setPreferredDate(request.getNewPreferredDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setRescheduleCount(
                (booking.getRescheduleCount() == null ? 0 : booking.getRescheduleCount()) + 1);
        booking.setLastRescheduledAt(OffsetDateTime.now());
        booking.setLastRescheduledReason(request.getReason());
        // Clear any reminder that was queued for the old date
        booking.setReminderSentForDate(null);

        Booking saved = bookingRepository.save(booking);

        auditService.log(patientId, "PATIENT", "BOOKING_RESCHEDULED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Patient {} rescheduled booking {} from {} to {} (reset to PENDING, count={})",
                patientId, bookingId, oldDate, request.getNewPreferredDate(), saved.getRescheduleCount());

        // Notify: patient sees the status-update (now PENDING for the new date),
        // and the hospital gets alerted to re-confirm.
        safelyNotify(() -> notificationService.notifyBookingStatusUpdateToPatient(saved),
                "reschedule patient notification");
        safelyNotify(() -> notificationService.notifyBookingAlertToHospital(saved),
                "reschedule hospital alert");

        return saved;
    }

    private void safelyNotify(Runnable action, String label) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Notification failed [{}]: {}", label, e.getMessage(), e);
        }
    }
}
