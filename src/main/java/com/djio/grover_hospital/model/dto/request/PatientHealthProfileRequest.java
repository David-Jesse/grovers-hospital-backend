package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Fields a patient is allowed to edit on their own health profile.
 * Notably excludes blood_group, genotype, allergies, clinical_notes -
 * those are admin-managed for data integrity
 */

@Data
public class PatientHealthProfileRequest {

    @Min(value = 30, message = "Height must be at least 30 cm")
    @Max(value = 300, message = "Height cannot exceed 300 cm")
    private Integer heightCm;

    @DecimalMin(value = "1.0", message = "Weight must be positive")
    private BigDecimal weightKg;

    @Size(max = 200)
    private String emergencyContactName;

    @Size(max = 100)
    private String emergencyContactRelationship;

    @Size(max = 20)
    private String emergencyContactPhone;
}
