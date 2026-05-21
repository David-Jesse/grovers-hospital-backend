package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.AccountDeletionRequestDto;
import com.djio.grover_hospital.model.dto.request.CancelDeletionRequest;
import com.djio.grover_hospital.model.dto.response.AccountDeletionStatusResponse;
import com.djio.grover_hospital.model.entity.AccountDeletionRequest;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.AccountDeletionStatus;
import com.djio.grover_hospital.repository.AccountDeletionRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Handles patient-initiated account deletion with a 30-day grace period
 * <p>
 * Flow:
 * 1. Patient Posts /portal/account/delete-request with password + optional reason
 * 2. We verify the password, create a PENDING_DELETION row scheduled for a now+30d
 * 3. Account stays usable during grace window
 * 4. Patient can Post /portal/account/cancel-deletion (with password) to abort.
 * 5. After 30 days, the daily cron hard-deletes the patient and all their related data
 * ( Cascade handles most via FK on delete cascade)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionService {

    private static final String RESOURCE_TYPE = "ACCOUNT_DELETION";
    private static final int GRACE_PERIOD_DAYS = 30;

    private final AccountDeletionRepository deletionRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ========= Request deletion ========

    @Transactional
    public AccountDeletionStatusResponse requestDeletion(AccountDeletionRequestDto request, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new BadRequestException("Password is incorrect");
        }

        // Reject duplicate pending requests
        deletionRepository.findByPatientIdAndStatus(patientId, AccountDeletionStatus.PENDING_DELETION)
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "An account deletion request is already pending. Scheduled for " + existing.getScheduledFor());
                });

        OffsetDateTime scheduledFor = OffsetDateTime.now().plusDays(GRACE_PERIOD_DAYS);

        com.djio.grover_hospital.model.entity.AccountDeletionRequest deletion = com.djio.grover_hospital.model.entity.AccountDeletionRequest.builder()
                .patient(patient)
                .status(AccountDeletionStatus.PENDING_DELETION)
                .reason(request.getReason())
                .scheduledFor(scheduledFor)
                .build();

        AccountDeletionRequest saved = deletionRepository.save(deletion);

        auditService.log(patientId, "PATIENT", "ACCOUNT_DELETION_REQUESTED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        log.info("Patient {} requested account deletion (scheduled for {}, reason='{}')",
                patientId, scheduledFor, request.getReason());

        return AccountDeletionStatusResponse.from(saved);
    }

    // ========== Cancel deletion =========

    @Transactional
    public void cancelDeletion(CancelDeletionRequest request, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new BadRequestException("Password is incorrect");
        }

        AccountDeletionRequest pending = deletionRepository
                .findByPatientIdAndStatus(patientId, AccountDeletionStatus.PENDING_DELETION)
                .orElseThrow(() -> new ResourceNotFoundException("No pending deletion request to cancel"));

        deletionRepository.delete(pending);

        auditService.log(patientId, "PATIENT", "ACCOUNT_DELETION_CANCELLED",
                RESOURCE_TYPE, pending.getId(), httpRequest
        );
        log.info("Patient {} cancelled pending account deletion {}", patientId, pending.getId());
    }

    // =======================================
    // Get current pending status (for UI)
    // ========================================

    @Transactional(readOnly = true)
    public AccountDeletionStatusResponse getMyPendingDeletion() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return deletionRepository
                .findByPatientIdAndStatus(patientId, AccountDeletionStatus.PENDING_DELETION)
                .map(AccountDeletionStatusResponse::from)
                .orElse(null);
    }

    // ============================================================
    // Cron-driven hard delete
    // ============================================================

    /**
     * Called by AccountDeletionCron daily. Finds all PENDING_DELETION requests
     * whose scheduled_for is in the past and hard-deletes the patient.
     * <p>
     * Patient.id deletion cascades through every table that uses
     * patient_id FK with ON DELETE CASCADE (bookings, results, medications,
     * chronic_conditions, visits, health_profiles, portal_notifications,
     * notification_preferences, feedback, data_export_jobs, this row itself).
     * <p>
     * Access logs use ON DELETE SET NULL on user_id, so they get anonymized
     * rather than deleted — preserves audit history.
     */

    @Transactional
    public int processExpiredDeletions() {
        OffsetDateTime now = OffsetDateTime.now();
        List<AccountDeletionRequest> expired = deletionRepository
                .findByStatusAndScheduledForBefore(AccountDeletionStatus.PENDING_DELETION, now);

        if (expired.isEmpty()) {
            return 0;
        }

        log.info("Account deletion cron: {} expired pending deletions to process", expired.size());

        int processed = 0;
        for (AccountDeletionRequest req : expired) {
            Long patientId = req.getPatient().getId();
            try {
                // Delete the patient - cascades through related tables
                patientRepository.deleteById(patientId);
                log.info("Hard-deleted patient {} via account deletion request {}", patientId, req.getId());
                processed++;
                // The AccountDeletionRequest row was cascade-deleted along with the patient,
                // so no explicit save needed here
            } catch (Exception e) {
                log.error("Failed to hard-delete patient {} (request {}): {}",
                        patientId, req.getId(), e.getMessage(), e
                );
                req.setStatus(AccountDeletionStatus.FAILED);
                req.setProcessedAt(now);
                deletionRepository.save(req);
            }
        }

        return processed;
    }

}
