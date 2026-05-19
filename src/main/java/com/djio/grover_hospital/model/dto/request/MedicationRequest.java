package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequest {

    @NotBlank(message = "Medication name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 100)
    private String dosage;

    @Size(max = 100)
    private String frequency;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isActive;

    private String notes;

    /** Optional internal doctor FK. If null, prescribedByText is used instead. */
    private Long prescribedById;

    @Size(max = 200)
    private String prescribedByText;
}