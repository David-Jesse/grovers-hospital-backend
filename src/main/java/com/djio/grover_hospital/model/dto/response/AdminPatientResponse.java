package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPatientResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String whatsappNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public static AdminPatientResponse from(Patient p) {
        return AdminPatientResponse.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .whatsappNumber(p.getWhatsappNumber())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .build();
    }
}