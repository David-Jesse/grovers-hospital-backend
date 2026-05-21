package com.djio.grover_hospital.util;

import com.djio.grover_hospital.service.AccountDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily sweep for expired pending account deletions. Runs at 02:00 Africa/Lagos
 * <p>
 * Requires @EnableScheduling somewhere in the Spring config - add it to your
 *
 * @SpringBootApplication main class if not already present
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionCron {

    private final AccountDeletionService accountDeletionService;

    /**
     * Cron: every day at 02:00:00 Lagos time.
     * Pattern: second, minute, hour, day-of-month, month, day-of-week
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Africa/Lagos")
    public void sweep() {
        log.debug("Account deletion cron starting");
        try {
            int processed = accountDeletionService.processExpiredDeletions();
            if (processed > 0) {
                log.info("Account deletion cron processed {} expired deletions", processed);
            }
        } catch (Exception e) {
            log.error("Account deletion cron failed: {}", e.getMessage(), e);
        }
    }
}