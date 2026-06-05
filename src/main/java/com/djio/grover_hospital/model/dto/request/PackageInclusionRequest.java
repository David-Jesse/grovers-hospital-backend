package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PackageInclusionRequest {

    @NotBlank(message = "Inclusion label is required")
    @Size(max = 255)
    private String label;

    private String description;

    private Integer displayOrder;
}