package com.djio.grover_hospital.model.enums;

/**
 * Lifecycle of a notification attempt.
 *
 * <p>Email transitions: QUEUED -> SENT (no further updates; SMTP has no protocol-level
 * delivery callbacks). Failures become FAILED.</p>
 *
 * <p>SMS transitions: QUEUED -> SENT -> DELIVERED (or FAILED) via SMPP DLR.</p>
 *
 * <p>WhatsApp transitions: QUEUED -> SENT (delivery semantics depend on provider once wired).</p>
 */
public enum DeliveryStatus {
    QUEUED,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED
}