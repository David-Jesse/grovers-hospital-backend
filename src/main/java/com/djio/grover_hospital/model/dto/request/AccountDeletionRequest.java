package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {

    @NotBlank(message = "Password is required to confirm deletion")
    private String password;

    /** Optional. Free-text */
    @Size(max = 100)
    private String reason;
}
