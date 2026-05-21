package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDeletionRequest {

    @NotBlank(message = "Password is required to cancel deletion")
    private String password;
}