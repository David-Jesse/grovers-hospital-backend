package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public-facing doctor view. Excludes contact details (email/phone) which
 * should only be exposed to admins, not patients or the public.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;
    private String fullName;
    private String title;
    private String specialty;
    private Long departmentId;
    private String departmentName;
    private String photoUrl;
    private String bio;

    public static DoctorResponse from(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .title(doctor.getTitle())
                .specialty(doctor.getSpecialty())
                .departmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null)
                .departmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null)
                .photoUrl(doctor.getPhotoUrl())
                .bio(doctor.getBio())
                .build();
    }
}