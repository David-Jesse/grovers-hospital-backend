package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin review payload. Sent to the approved and reject endpoints.
 * adminResponse is optional context shown back to the patient.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProfileUpdateRequestDto {

    @Size(max = 2000)
    private String adminResponse;
}