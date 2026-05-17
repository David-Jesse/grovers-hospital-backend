package com.djio.grover_hospital.service;


import com.djio.grover_hospital.repository.PortalNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Removes in-portal notifications older than 90 days
 * Runs once a day at 3 AM local server time (low-traffic window)
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class PortalNotificationCleanupJob {

    private static final int RETENTION_DAYS = 90;

    private final PortalNotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *") // runs everyday at 3:00 AM
    @Transactional
    public void cleanupNotifications() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = notificationRepository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Cleanup: deleted {} portal notification older than {} days", deleted, RETENTION_DAYS);
        }
    }
}