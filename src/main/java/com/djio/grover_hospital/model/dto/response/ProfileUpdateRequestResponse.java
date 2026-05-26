package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.entity.ProfileUpdateRequest;
import com.djio.grover_hospital.model.enums.ProfileUpdateField;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequestResponse {

    private Long id;
    private Long patientId;
    private String patientName;

    private ProfileUpdateField targetField;
    private String otherFieldDescription;
    private String currentValue;
    private String proposedValue;
    private String patientNote;

    private ProfileUpdateStatus status;

    private Long reviewedByAdminId;
    private String reviewedByAdminName;
    private OffsetDateTime reviewedAt;
    private String adminResponse;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ProfileUpdateRequestResponse from(ProfileUpdateRequest r) {
        Patient p = r.getPatient();
        Admin reviewer = r.getReviewedByAdmin();
        return ProfileUpdateRequestResponse.builder()
                .id(r.getId())
                .patientId(p != null ? p.getId() : null)
                .patientName(p != null ? p.getFirstName() + " " + p.getLastName() : null)
                .targetField(r.getTargetField())
                .otherFieldDescription(r.getOtherFieldDescription())
                .currentValue(r.getCurrentValue())
                .proposedValue(r.getProposedValue())
                .patientNote(r.getPatientNote())
                .status(r.getStatus())
                .reviewedByAdminId(reviewer != null ? reviewer.getId() : null)
                .reviewedByAdminName(reviewer != null ? reviewer.getFullName() : null)
                .reviewedAt(r.getReviewedAt())
                .adminResponse(r.getAdminResponse())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}