package com.djio.grover_hospital.notification.sendchamp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** A named email address in Sendchamp's documented email contract. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendchampEmailAddress(String email, String name) {
}
