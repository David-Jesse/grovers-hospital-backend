package com.djio.grover_hospital.scheduler;

import com.djio.grover_hospital.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Daily sweep: drop reset tokens older than 24 hours. Tokens expire after 30 minutes
 * functionally, but we keep them around for one day so used_at records persist briefly
 * for audit/debugging. Beyond that, they're noise.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetTokenCleanupScheduler {

    private final PasswordResetTokenRepository repository;

    // 03:00 Lagos. Spring's relaxed cron picks up the existing timezone config.
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Lagos")
    @Transactional
    public void purgeOldTokens() {
        int removed = repository.deleteOlderThan(OffsetDateTime.now().minusHours(24));
        if (removed > 0) {
            log.info("Password reset cleanup: removed {} expired tokens", removed);
        }
    }
}