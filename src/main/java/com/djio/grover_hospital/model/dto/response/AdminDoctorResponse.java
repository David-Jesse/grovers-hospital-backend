package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorResponse {

    private Long id;
    private String fullName;
    private String title;
    private String specialty;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private String photoUrl;
    private String bio;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminDoctorResponse from(Doctor doctor) {
        return AdminDoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .title(doctor.getTitle())
                .specialty(doctor.getSpecialty())
                .departmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null)
                .departmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null)
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .photoUrl(doctor.getPhotoUrl())
                .bio(doctor.getBio())
                .isActive(doctor.getIsActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}