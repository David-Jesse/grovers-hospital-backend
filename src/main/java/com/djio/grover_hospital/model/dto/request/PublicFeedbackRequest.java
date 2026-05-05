package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublicFeedbackRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 300)
    private String subject;

    @NotBlank(message = "message is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;
}
