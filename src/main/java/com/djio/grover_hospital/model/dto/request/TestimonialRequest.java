package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestimonialRequest {

    @NotBlank(message = "Patient name is required")
    @Size(max = 150)
    private String patientName;

    @NotBlank(message = "Content is required")
    private String content;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private Integer displayOrder;

    private Boolean isApproved;
}