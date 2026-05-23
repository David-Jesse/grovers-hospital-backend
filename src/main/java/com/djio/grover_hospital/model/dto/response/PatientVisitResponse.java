package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Visit;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Patient-facing view of one of their own past visits. Mirrors the admin
 * VisitResponse but is named distinctly so the patient surface is explicit.
 * Patients see the clinical content of their own visit (diagnosis, treatment, notes) - it's their own medical record.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientVisitResponse {

    private Long id;
    private Long bookingId;
    private LocalDate visitDate;
    private String chiefComplaint;
    private String diagnosis;
    private String treatment;
    private String clinicalNotes;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private String attendingDoctorName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PatientVisitResponse from(Visit v) {
        Doctor doc = v.getAttendingDoctor();
        String resolvedName = (doc != null) ? doc.getFullName() : v.getAttendingDoctorText();
        return PatientVisitResponse.builder()
                .id(v.getId())
                .bookingId(v.getBooking() != null ? v.getBooking().getId() : null)
                .visitDate(v.getVisitDate())
                .chiefComplaint(v.getChiefComplaint())
                .diagnosis(v.getDiagnosis())
                .treatment(v.getTreatment())
                .clinicalNotes(v.getClinicalNotes())
                .followUpRequired(v.getFollowUpRequired())
                .attendingDoctorName(resolvedName)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
