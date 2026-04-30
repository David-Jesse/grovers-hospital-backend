package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamMemberRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 200)
    private String fullName;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String bio;

    @Size(max = 500)
    private String photoUrl;

    private Integer displayOrder;

    private Boolean isActive;
}
