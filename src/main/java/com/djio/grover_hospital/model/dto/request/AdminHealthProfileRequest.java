package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminHealthProfileRequest {

    @Pattern(regexp = "^(A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-)?$",
            message = "Blood group must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-")
    @Size(max = 5)
    private String bloodGroup;

    @Pattern(regexp = "^(AA|AS|AC|SS|SC)?$",
            message = "Genotype must be one of: AA, AS, AC, SS, SC")
    @Size(max = 5)
    private String genotype;

    private String allergies;

    private String clinicalNotes;

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