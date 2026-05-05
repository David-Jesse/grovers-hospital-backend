package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateResultRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    /**
     * Optional - link to a specific booking if relevant
     */
    private Long bookingId;

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    private String title;

    private String description;

    /**
     * If true, send notification to the patient immediately
     * Default true. Admin can set false to upload silently and notify later
     */
    private Boolean notifyPatient;
}
