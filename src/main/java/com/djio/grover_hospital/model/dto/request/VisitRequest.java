package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitRequest {

    private LocalDate visitDate;
    private String chiefComplaint;
    private String diagnosis;
    private String treatment;
    private String clinicalNotes;
    private Boolean followUpRequired;
    private LocalDate followUpDate;

    /** Optional internal doctor FK. If null, attendingDoctorText is used instead. */
    private Long attendingDoctorId;

    @Size(max = 200)
    private String attendingDoctorText;
}