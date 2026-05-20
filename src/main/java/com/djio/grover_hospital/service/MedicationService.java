package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.MedicationRequest;
import com.djio.grover_hospital.model.dto.response.MedicationResponse;
import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Medication;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.repository.DoctorRepository;
import com.djio.grover_hospital.repository.MedicationRepository;
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
public class MedicationService {

    private static final String RESOURCE_TYPE = "MEDICATION";

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    // ====== Patient Facing ======

    @Transactional
    public List<MedicationResponse> getMyMedications(Boolean activeOnly) {
        Long patientId = SecurityUtils.getCurrentUserId();
        List<Medication> meds = (activeOnly != null && activeOnly)
                ? medicationRepository.findByPatientIdAndIsActiveOrderByStartDateDesc(patientId, true)
                : medicationRepository.findByPatientIdOrderByIsActiveDescStartDateDesc(patientId);
        return meds.stream().map(MedicationResponse::from).toList();
    }

    // ======= Admin Facing =======

    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedicationsForPatient(Long patientId) {
        verifyPatientExists(patientId);
        return medicationRepository.findByPatientIdOrderByIsActiveDescStartDateDesc(patientId)
                .stream().map(MedicationResponse::from).toList();
    }

    @Transactional
    public MedicationResponse createForPatient(Long patientId, MedicationRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        Medication med = Medication.builder()
                .patient(patient)
                .name(request.getName())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive())
                .notes(request.getNotes())
                .prescribedBy(resolveDoctor(request.getPrescribedById()))
                .prescribedByText(request.getPrescribedByText())
                .build();

        Medication saved = medicationRepository.save(med);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "MEDICATION_CREATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        log.info("Admin {} created medication {} for patient {}", adminId, saved.getId(), patientId);

        return MedicationResponse.from(saved);
    }

    @Transactional
    public MedicationResponse update(Long medicationId, MedicationRequest request, HttpServletRequest httpRequest) {
        Medication med = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id " + medicationId));

        med.setName(request.getName());
        med.setDosage(request.getDosage());
        med.setFrequency(request.getFrequency());
        med.setStartDate(request.getStartDate());
        med.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            med.setIsActive(request.getIsActive());
        }
        med.setNotes(request.getNotes());
        med.setPrescribedBy(resolveDoctor(request.getPrescribedById()));
        med.setPrescribedByText(request.getPrescribedByText());

        Medication saved = medicationRepository.save(med);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "MEDICATION_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );

        return MedicationResponse.from(saved);
    }

    @Transactional
    public void delete(Long medicationId, HttpServletRequest httpRequest) {
        Medication med = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id " + medicationId));

        Long patientId = med.getPatient().getId();
        medicationRepository.delete(med);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "MEDICATION_DELETED",
                RESOURCE_TYPE, medicationId, httpRequest
        );
        log.info("Admin {} deleted medication {} for patient {}", adminId, medicationId, patientId);
    }

    // ===== Helpers ======
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