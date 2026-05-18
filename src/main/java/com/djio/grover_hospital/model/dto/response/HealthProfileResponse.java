package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.HealthProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Patient-facing view of their own health profile.
 * Excludes clinical_notes (internal-only admin field).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileResponse {

    private String bloodGroup;
    private String genotype;
    private String allergies;
    private Integer heightCm;
    private BigDecimal weightKg;
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;
    private OffsetDateTime updatedAt;

    public static HealthProfileResponse from(HealthProfile profile) {
        if (profile == null) return null;
        return HealthProfileResponse.builder()
                .bloodGroup(profile.getBloodGroup())
                .genotype(profile.getGenotype())
                .allergies(profile.getAllergies())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /** Returns an empty profile shape when the patient has no profile yet. */
    public static HealthProfileResponse empty() {
        return HealthProfileResponse.builder().build();
    }
}