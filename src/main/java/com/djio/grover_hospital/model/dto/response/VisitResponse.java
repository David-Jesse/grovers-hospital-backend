package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Visit;
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
public class VisitResponse {

    private Long id;
    private Long bookingId;
    private Long patientId;
    private String patientName;

    private LocalDate visitDate;
    private String chiefComplaint;
    private String diagnosis;
    private String treatment;
    private String clinicalNotes;
    private Boolean followUpRequired;
    private LocalDate followUpDate;

    private Long attendingDoctorId;
    private String attendingDoctorName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static VisitResponse from(Visit v) {
        Doctor doc = v.getAttendingDoctor();
        String resolvedName = (doc != null) ? doc.getFullName() : v.getAttendingDoctorText();
        return VisitResponse.builder()
                .id(v.getId())
                .bookingId(v.getBooking() != null ? v.getBooking().getId() : null)
                .patientId(v.getPatient() != null ? v.getPatient().getId() : null)
                .patientName(
                        v.getPatient() != null
                                ? v.getPatient().getFirstName() + " " + v.getPatient().getLastName()
                                : null
                )
                .visitDate(v.getVisitDate())
                .chiefComplaint(v.getChiefComplaint())
                .diagnosis(v.getDiagnosis())
                .treatment(v.getTreatment())
                .clinicalNotes(v.getClinicalNotes())
                .followUpRequired(v.getFollowUpRequired())
                .followUpDate(v.getFollowUpDate())
                .attendingDoctorId(doc != null ? doc.getId() : null)
                .attendingDoctorName(resolvedName)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}