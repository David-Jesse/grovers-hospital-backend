package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.AdminHealthProfileRequest;
import com.djio.grover_hospital.model.dto.request.PatientHealthProfileRequest;
import com.djio.grover_hospital.model.dto.response.AdminHealthProfileResponse;
import com.djio.grover_hospital.model.dto.response.HealthProfileResponse;
import com.djio.grover_hospital.model.entity.HealthProfile;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.repository.HealthProfileRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing patient health profiles.
 *
 * Patient-facing methods (getMyProfile / updateMyProfile) only allow editing
 * self-reported fields (height, weight, emergency contact). Clinical fields
 * (bloodGroup, genotype, allergies, clinicalNotes) are admin-only.
 *
 * Profiles are auto-created on first read so the patient portal never sees a 404.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProfileService {

    private static final String RESOURCE_TYPE = "HEALTH_PROFILE";

    private final HealthProfileRepository healthProfileRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    // ============================================================
    // Patient-facing methods
    // ============================================================

    @Transactional
    public HealthProfileResponse getMyProfile() {
        Long patientId = SecurityUtils.getCurrentUserId();
        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfileFor(patientId));
        return HealthProfileResponse.from(profile);
    }

    @Transactional
    public HealthProfileResponse updateMyProfile(PatientHealthProfileRequest request,
                                                 HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfileFor(patientId));

        // Only self-reported fields — clinical fields are admin-only
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        HealthProfile saved = healthProfileRepository.save(profile);
        auditService.log(patientId, "PATIENT", "HEALTH_PROFILE_UPDATED_BY_PATIENT",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Patient {} updated own health profile (id={})", patientId, saved.getId());
        return HealthProfileResponse.from(saved);
    }

    // ============================================================
    // Admin-facing methods
    // ============================================================

    @Transactional
    public AdminHealthProfileResponse getProfileForPatient(Long patientId, HttpServletRequest httpRequest) {
        // Verify patient exists before any auto-create
        patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfileFor(patientId));

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "HEALTH_PROFILE_VIEWED_BY_ADMIN",
                RESOURCE_TYPE, profile.getId(), httpRequest);

        return AdminHealthProfileResponse.from(profile);
    }

    @Transactional
    public AdminHealthProfileResponse updateProfileForPatient(Long patientId,
                                                              AdminHealthProfileRequest request,
                                                              HttpServletRequest httpRequest) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfileFor(patientId));

        // Admin can touch everything
        profile.setBloodGroup(request.getBloodGroup());
        profile.setGenotype(request.getGenotype());
        profile.setAllergies(request.getAllergies());
        profile.setClinicalNotes(request.getClinicalNotes());
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        HealthProfile saved = healthProfileRepository.save(profile);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "HEALTH_PROFILE_UPDATED_BY_ADMIN",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} updated health profile for patient {} (profile id={})", adminId, patientId, saved.getId());

        return AdminHealthProfileResponse.from(saved);
    }

    // ============================================================
    // Internal helpers
    // ============================================================

    private HealthProfile createEmptyProfileFor(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));
        HealthProfile profile = HealthProfile.builder()
                .patient(patient)
                .build();
        HealthProfile saved = healthProfileRepository.save(profile);
        log.info("Auto-created empty health profile for patient {} (profile id={})", patientId, saved.getId());
        return saved;
    }
}