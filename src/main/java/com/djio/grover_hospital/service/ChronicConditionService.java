package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.ChronicConditionRequest;
import com.djio.grover_hospital.model.dto.response.ChronicConditionResponse;
import com.djio.grover_hospital.model.entity.ChronicCondition;
import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.ConditionStatus;
import com.djio.grover_hospital.repository.ChronicConditionRepository;
import com.djio.grover_hospital.repository.DoctorRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChronicConditionService {

    private static final String RESOURCE_TYPE = "CHRONIC_CONDITION";

    private final ChronicConditionRepository chronicConditionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    // ============ Patient-facing ==========

    @Transactional
    public List<ChronicConditionResponse> getMyConditions() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return chronicConditionRepository.findByPatientIdOrderByDiagnosedDateDesc(patientId)
                .stream().map(ChronicConditionResponse::from).toList();
    }

    // ======= Admin Facing ======

    @Transactional
    public List<ChronicConditionResponse> getConditionsForPatient(Long patientId) {
        verifyPatientExists(patientId);
        return chronicConditionRepository.findByPatientIdOrderByDiagnosedDateDesc(patientId)
                .stream().map(ChronicConditionResponse::from).toList();
    }

    @Transactional
    public ChronicConditionResponse createForPatient(Long patientId, ChronicConditionRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        ChronicCondition condition = ChronicCondition.builder()
                .patient(patient)
                .name(request.getName())
                .diagnosedDate(request.getDiagnosedDate())
                .status(request.getStatus() == null ? ConditionStatus.ACTIVE : request.getStatus())
                .notes(request.getNotes())
                .managingDoctor(resolveDoctor(request.getManagingDoctorId()))
                .managingDoctorText(request.getManagingDoctorText())
                .build();

        ChronicCondition saved = chronicConditionRepository.save(condition);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "CHRONIC_CONDITION_CREATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        log.info("Admin {} created chronic condition {} for patient {}", adminId, saved.getId(), patientId);

        return ChronicConditionResponse.from(saved);
    }

    @Transactional
    public ChronicConditionResponse update(Long conditionId, ChronicConditionRequest request, HttpServletRequest httpRequest) {
        ChronicCondition condition = chronicConditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chronic condition not found with id " + conditionId));

        condition.setName(request.getName());
        condition.setDiagnosedDate(request.getDiagnosedDate());
        if (request.getStatus() != null) {
            condition.setStatus(request.getStatus());
        }
        condition.setNotes(request.getNotes());
        condition.setManagingDoctor(resolveDoctor(request.getManagingDoctorId()));
        condition.setManagingDoctorText(request.getManagingDoctorText());

        ChronicCondition saved = chronicConditionRepository.save(condition);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "CHRONIC_CONDITION_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        return ChronicConditionResponse.from(saved);
    }

    @Transactional
    public void delete(Long conditionId, HttpServletRequest httpRequest) {
        ChronicCondition condition = chronicConditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chronic condition not found with id " + conditionId));
        Long patientId = condition.getPatient().getId();
        chronicConditionRepository.delete(condition);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "CHRONIC_CONDITION_DELETED",
                RESOURCE_TYPE, conditionId, httpRequest
        );
        log.info("Admin {} deleted chronic condition {} for patient {}", adminId, conditionId, patientId);
    }

    // Helpers

    private Doctor resolveDoctor(Long doctorId) {
        if (doctorId == null) return null;
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));
    }

    private void verifyPatientExists(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
    }
}
