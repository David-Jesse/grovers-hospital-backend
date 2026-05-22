package com.djio.grover_hospital.util;

import com.djio.grover_hospital.service.AppointmentReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily 24-hour appointment reminder sweep. Runs at 09:00 Africa/Lagos —
 * morning-before reminders for next-day appointments.
 * <p>
 * Requires @EnableScheduling (already added in Batch 13B-2 for the
 * account-deletion cron).
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderCron {

    private final AppointmentReminderService appointmentReminderService;

    /**
     * Cron: every day at 09:00:00 Lagos time.
     * Pattern: second, minute, hour, day-of-month, month, day-of-week
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Africa/Lagos")
    public void sweep() {
        log.debug("Appointment reminder cron starting");
        try {
            int sent = appointmentReminderService.sendRemindersForTomorrow();
            if (sent > 0) {
                log.info("Appointment reminder cron dispatched {] reminders", sent);
            }
        } catch (Exception e) {
            log.error("Appointment reminder cron failed: {}", e.getMessage(), e);
        }
    }
}
