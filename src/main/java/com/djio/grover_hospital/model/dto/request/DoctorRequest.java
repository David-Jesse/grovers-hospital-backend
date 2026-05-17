package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 200)
    private String fullName;

    @Size(max = 20)
    private String title;

    @Size(max = 200)
    private String specialty;

    private Long departmentId;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String photoUrl;

    private String bio;

    private Boolean isActive;
}