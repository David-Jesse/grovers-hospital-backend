package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.CreateProfileUpdateRequestDto;
import com.djio.grover_hospital.model.dto.request.ReviewProfileUpdateRequestDto;
import com.djio.grover_hospital.model.dto.response.ProfileUpdateRequestResponse;
import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.HealthProfile;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.entity.ProfileUpdateRequest;
import com.djio.grover_hospital.model.enums.ProfileUpdateField;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.HealthProfileRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.repository.ProfileUpdateRequestRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileUpdateRequestService {

    private static final String RESOURCE_TYPE = "PROFILE_UPDATE_REQUEST";

    private final ProfileUpdateRequestRepository requestRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final AuditService auditService;

    // ============================================================
    // Patient — submit + view own
    // ============================================================

    @Transactional
    public ProfileUpdateRequestResponse submit(CreateProfileUpdateRequestDto dto, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        // OTHER requires a description so the admin knows what field is meant
        if (dto.getTargetField() == ProfileUpdateField.OTHER
                && (dto.getOtherFieldDescription() == null || dto.getOtherFieldDescription().isBlank())) {
            throw new BadRequestException("otherFieldDescription is required when targetField is OTHER");
        }

        // Snapshot the current value (for known fields) so the admin sees old vs proposed
        String currentValue = snapshotCurrentValue(patientId, dto.getTargetField());

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .patient(patient)
                .targetField(dto.getTargetField())
                .otherFieldDescription(dto.getOtherFieldDescription())
                .currentValue(currentValue)
                .proposedValue(dto.getProposedValue())
                .patientNote(dto.getPatientNote())
                .status(ProfileUpdateStatus.PENDING)
                .build();

        ProfileUpdateRequest saved = requestRepository.save(request);

        auditService.log(patientId, "PATIENT", "PROFILE_UPDATE_REQUESTED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Patient {} submitted profile update request {} for field {}",
                patientId, saved.getId(), dto.getTargetField());

        return ProfileUpdateRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProfileUpdateRequestResponse> getMyRequests() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return requestRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(ProfileUpdateRequestResponse::from).toList();
    }

    // ============================================================
    // Admin — list + approve/reject
    // ============================================================

    @Transactional(readOnly = true)
    public Page<ProfileUpdateRequestResponse> listForAdmin(ProfileUpdateStatus status, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findForAdmin(status, pageable)
                .map(ProfileUpdateRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public ProfileUpdateRequestResponse getByIdForAdmin(Long requestId) {
        ProfileUpdateRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Update request not found with id " + requestId));
        return ProfileUpdateRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public ProfileUpdateRequestResponse getMyRequestById(Long requestId) {
        Long patientId = SecurityUtils.getCurrentUserId();
        ProfileUpdateRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Update request not found with id " + requestId));

        if (!request.getPatient().getId().equals(patientId)) {
            // Return 404 rather than 403 — don't confirm the resource exists
            throw new ResourceNotFoundException("Update request not found with id " + requestId);
        }

        return ProfileUpdateRequestResponse.from(request);
    }

    @Transactional
    public ProfileUpdateRequestResponse approve(Long requestId, ReviewProfileUpdateRequestDto dto,
                                                HttpServletRequest httpRequest) {
        ProfileUpdateRequest request = loadPending(requestId);
        Admin admin = currentAdmin();

        // Apply the change to the health profile for known clinical fields.
        // OTHER has no field to apply — approval just marks it handled and the
        // admin makes the actual edit manually.
        if (request.getTargetField() != ProfileUpdateField.OTHER) {
            applyToHealthProfile(request.getPatient().getId(), request.getTargetField(), request.getProposedValue());
        }

        request.setStatus(ProfileUpdateStatus.APPROVED);
        request.setReviewedByAdmin(admin);
        request.setReviewedAt(OffsetDateTime.now());
        request.setAdminResponse(dto != null ? dto.getAdminResponse() : null);

        ProfileUpdateRequest saved = requestRepository.save(request);

        auditService.log(admin.getId(), "ADMIN", "PROFILE_UPDATE_APPROVED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} approved profile update request {} (field {})",
                admin.getId(), requestId, request.getTargetField());

        return ProfileUpdateRequestResponse.from(saved);
    }

    @Transactional
    public ProfileUpdateRequestResponse reject(Long requestId, ReviewProfileUpdateRequestDto dto,
                                               HttpServletRequest httpRequest) {
        ProfileUpdateRequest request = loadPending(requestId);
        Admin admin = currentAdmin();

        request.setStatus(ProfileUpdateStatus.REJECTED);
        request.setReviewedByAdmin(admin);
        request.setReviewedAt(OffsetDateTime.now());
        request.setAdminResponse(dto != null ? dto.getAdminResponse() : null);

        ProfileUpdateRequest saved = requestRepository.save(request);

        auditService.log(admin.getId(), "ADMIN", "PROFILE_UPDATE_REJECTED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} rejected profile update request {} (field {})",
                admin.getId(), requestId, request.getTargetField());

        return ProfileUpdateRequestResponse.from(saved);
    }

    // ============================================================
    // Internal helpers
    // ============================================================

    private ProfileUpdateRequest loadPending(Long requestId) {
        ProfileUpdateRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Update request not found with id " + requestId));
        if (request.getStatus() != ProfileUpdateStatus.PENDING) {
            throw new BadRequestException("This request has already been " + request.getStatus().name().toLowerCase());
        }
        return request;
    }

    private Admin currentAdmin() {
        Long adminId = SecurityUtils.getCurrentUserId();
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin session is invalid"));
    }

    /**
     * Reads the field's current value off the patient's health profile (null if no profile yet).
     */
    private String snapshotCurrentValue(Long patientId, ProfileUpdateField field) {
        return healthProfileRepository.findByPatientId(patientId)
                .map(hp -> readField(hp, field))
                .orElse(null);
    }

    private String readField(HealthProfile hp, ProfileUpdateField field) {
        return switch (field) {
            case BLOOD_GROUP -> hp.getBloodGroup();
            case GENOTYPE -> hp.getGenotype();
            case ALLERGIES -> hp.getAllergies();
            case OTHER -> null;
        };
    }

    /**
     * Applies an approved value to the patient's health profile. Auto-creates the profile if missing.
     */
    private void applyToHealthProfile(Long patientId, ProfileUpdateField field, String value) {
        HealthProfile hp = healthProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    Patient patient = patientRepository.findById(patientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));
                    return HealthProfile.builder().patient(patient).build();
                });

        switch (field) {
            case BLOOD_GROUP -> hp.setBloodGroup(value);
            case GENOTYPE -> hp.setGenotype(value);
            case ALLERGIES -> hp.setAllergies(value);
            case OTHER -> { /* no-op — handled manually by admin */ }
        }

        healthProfileRepository.save(hp);
        log.info("Applied approved profile update to patient {} field {}", patientId, field);
    }
}