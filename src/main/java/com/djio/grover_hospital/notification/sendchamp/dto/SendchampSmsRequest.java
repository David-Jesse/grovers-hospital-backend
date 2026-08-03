package com.djio.grover_hospital.notification.sendchamp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Documented request contract for POST /sms/send. */
public record SendchampSmsRequest(
        List<String> to,
        String message,
        @JsonProperty("sender_name") String senderName,
        @JsonInclude(JsonInclude.Include.NON_NULL) String route
) {
}
