package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HealthPackageRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @Size(max = 500)
    private String targetAudience;

    private Long departmentId;

    private Integer displayOrder;

    private Boolean isActive;
}
