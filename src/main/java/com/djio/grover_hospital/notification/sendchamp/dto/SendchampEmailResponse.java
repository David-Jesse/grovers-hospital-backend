package com.djio.grover_hospital.notification.sendchamp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Confirmed Sendchamp email response envelope. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SendchampEmailResponse(
        Integer code,
        Data data,
        Object errors,
        String message,
        String status
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String id,
            String subject,
            String email,
            String status
    ) {
    }
}
