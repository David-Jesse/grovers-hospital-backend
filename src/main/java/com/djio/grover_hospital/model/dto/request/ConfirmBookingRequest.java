package com.djio.grover_hospital.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ConfirmBookingRequest {

    @NotNull(message = "Appointment time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    /** Optional internal note saved alongside the confirmation. Never shown to patient. */
    private String adminNotes;
}