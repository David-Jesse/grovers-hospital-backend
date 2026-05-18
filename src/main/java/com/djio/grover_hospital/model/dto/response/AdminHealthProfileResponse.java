package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.HealthProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Admin-facing view of a patient's health profile.
 * Adds clinical_notes (admin-only) and patient identifier metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminHealthProfileResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;

    private String bloodGroup;
    private String genotype;
    private String allergies;
    private String clinicalNotes;

    private Integer heightCm;
    private BigDecimal weightKg;

    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminHealthProfileResponse from(HealthProfile profile) {
        return AdminHealthProfileResponse.builder()
                .id(profile.getId())
                .patientId(profile.getPatient().getId())
                .patientName(profile.getPatient().getFirstName() + " " + profile.getPatient().getLastName())
                .patientEmail(profile.getPatient().getEmail())
                .bloodGroup(profile.getBloodGroup())
                .genotype(profile.getGenotype())
                .allergies(profile.getAllergies())
                .clinicalNotes(profile.getClinicalNotes())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}