package com.djio.grover_hospital.notification.sendchamp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Sendchamp documents HTML content type for both HTML and plain-text values. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendchampEmailBody(String type, String value) {
}
