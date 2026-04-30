package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @Size(max = 150)
    private String iconUrl;

    private Integer displayOrder;

    private Boolean isActive;
}
