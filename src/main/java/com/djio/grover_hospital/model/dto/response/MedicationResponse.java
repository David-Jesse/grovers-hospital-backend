package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Medication;
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
public class MedicationResponse {

    private Long id;
    private String name;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String notes;

    /** Set if the medication is linked to an internal doctor record. */
    private Long prescribedById;
    /** Resolved display name: doctor's name if FK is set, otherwise the free-text fallback. */
    private String prescribedByName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static MedicationResponse from(Medication m) {
        Doctor doc = m.getPrescribedBy();
        String resolvedName = (doc != null) ? doc.getFullName() : m.getPrescribedByText();
        return MedicationResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .dosage(m.getDosage())
                .frequency(m.getFrequency())
                .startDate(m.getStartDate())
                .endDate(m.getEndDate())
                .isActive(m.getIsActive())
                .notes(m.getNotes())
                .prescribedById(doc != null ? doc.getId() : null)
                .prescribedByName(resolvedName)
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}