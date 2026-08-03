package com.djio.grover_hospital.notification.sendchamp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Documented request contract for POST /email/send. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendchampEmailRequest(
        String subject,
        List<SendchampEmailAddress> to,
        SendchampEmailAddress from,
        @JsonProperty("message_body") SendchampEmailBody messageBody
) {
}
