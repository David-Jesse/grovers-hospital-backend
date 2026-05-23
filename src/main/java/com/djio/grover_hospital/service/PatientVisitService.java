package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.response.PatientVisitResponse;
import com.djio.grover_hospital.model.entity.Visit;
import com.djio.grover_hospital.repository.VisitRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Patient-facing read access to their own past visits
 * <p>
 * Visits are auto-stubbed when a booking is COMPLETED (Batch 12C) and filled
 * in by an admin. Empty stubs (no visitDate / no clinical content yet) are
 * filtered out of the patient list so patients only see visits the clinic has actually
 * documented
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientVisitService {

    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public List<PatientVisitResponse> getMyVisits() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId)
                .stream()
                .filter(this::isDocumented)
                .map(PatientVisitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientVisitResponse getMyVisit(Long visitId) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id " + visitId));

        if (visit.getPatient() == null || !visit.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You can only view your own visits");
        }

        return PatientVisitResponse.from(visit);
    }

    /**
     * A visit is "documented" (worth showing the patient) once it has at least
     * a visit date or some clinical content. Pure empty stubs are hidden
     */
    private boolean isDocumented(Visit v) {
        return v.getVisitDate() != null
                || hasText(v.getChiefComplaint())
                || hasText(v.getDiagnosis())
                || hasText(v.getTreatment())
                || hasText(v.getClinicalNotes());
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
