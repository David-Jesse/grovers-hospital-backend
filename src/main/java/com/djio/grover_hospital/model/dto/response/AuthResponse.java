package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Role role;
    private String fullName;
    private String email;
    private Long accessExpirationMs;

    public AuthResponse(String accessToken, String refreshToken, long accessExpirationMs, Role role,
                        String fullName, String email
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.accessExpirationMs = accessExpirationMs;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }
}
