package com.djio.grover_hospital.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled tasks across the application.
 * Currently used by:
 *  - PortalNotificationCleanupJob (daily at 3 AM, deletes notifications older than 90 days)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}