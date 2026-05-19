package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.ChronicCondition;
import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.enums.ConditionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChronicConditionResponse {

    private Long id;
    private String name;
    private LocalDate diagnosedDate;
    private ConditionStatus status;
    private String notes;

    private Long managingDoctorId;
    private String managingDoctorName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ChronicConditionResponse from(ChronicCondition c) {
        Doctor doc = c.getManagingDoctor();
        String resolvedName = (doc != null) ? doc.getFullName() : c.getManagingDoctorText();
        return ChronicConditionResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .diagnosedDate(c.getDiagnosedDate())
                .status(c.getStatus())
                .notes(c.getNotes())
                .managingDoctorId(doc != null ? doc.getId() : null)
                .managingDoctorName(resolvedName)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}