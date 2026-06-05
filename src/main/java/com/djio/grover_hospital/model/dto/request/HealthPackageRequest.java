package com.djio.grover_hospital.model.dto.request;


import com.djio.grover_hospital.model.enums.Tone;
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

    /**
     * Visual tone for the heading band on the public page. Optional on create/update —
     * defaults to GREEN at the entity level if not supplied.
     */
    private Tone headingTone;

    /**
     * Visual tone for the pricing band on the public page. Optional on create/update —
     * defaults to GREEN at the entity level if not supplied.
     */
    private Tone pricingTone;
}