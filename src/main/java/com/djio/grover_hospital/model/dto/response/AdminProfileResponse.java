package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;

    public static AdminProfileResponse from(Admin admin) {
        return AdminProfileResponse.builder()
                .id(admin.getId())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .build();
    }
}