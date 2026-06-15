package com.djio.grover_hospital.notification.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Outcome of a single send attempt at the provider edge.
 * Does NOT represent final delivery — only that the provider accepted (or rejected) the submission.
 */
@Getter
@Builder
@AllArgsConstructor
public class SendResult {

    private final boolean success;

    /** Provider-side identifier (SMPP message_id, SMTP message-id header, etc.) when available. */
    private final String providerMessageId;

    /** Human-readable error message if success is false. Null otherwise. */
    private final String errorMessage;

    public static SendResult success(String providerMessageId) {
        return SendResult.builder()
                .success(true)
                .providerMessageId(providerMessageId)
                .build();
    }

    public static SendResult failure(String errorMessage) {
        return SendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}