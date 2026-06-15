package com.djio.grover_hospital.notification.core;

/**
 * Channel-specific sender for outbound email.
 * Implementations are selected at startup based on app.notification.email.provider.
 *
 * <p>Implementations MUST NOT throw — they must catch all transport errors and return
 * SendResult.failure(...). This keeps the notification pipeline simple and avoids
 * partial-state issues when one channel fails.</p>
 */
public interface EmailSender {
    SendResult send(EmailMessage message);
}