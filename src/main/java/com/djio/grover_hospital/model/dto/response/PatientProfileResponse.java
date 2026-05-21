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
public class PatientProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String whatsappNumber;
    private String gender;
    private OffsetDateTime memberSince;

    public static PatientProfileResponse from(Patient patient) {
        return PatientProfileResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .whatsappNumber(patient.getWhatsappNumber())
                .memberSince(patient.getCreatedAt())
                .build();
    }
}