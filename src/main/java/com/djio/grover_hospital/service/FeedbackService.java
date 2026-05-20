package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.AdminFeedbackStatusUpdateRequest;
import com.djio.grover_hospital.model.dto.request.PortalFeedbackRequest;
import com.djio.grover_hospital.model.dto.request.PublicFeedbackRequest;
import com.djio.grover_hospital.model.dto.response.AdminFeedbackResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackStats;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.FeedbackRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedbackService {

    private static final String RESOURCE_TYPE = "FEEDBACK";

    private final FeedbackRepository feedbackRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // ============================================================
    // Public homepage submission
    // ============================================================
    @Transactional
    public FeedbackResponse submitPublicFeedback(PublicFeedbackRequest request) {
        Feedback feedback = Feedback.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .subject(request.getSubject() != null ? request.getSubject().trim() : null)
                .message(request.getMessage().trim())
                .isRead(false)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Public feedback #{} submitted from {}", saved.getId(), saved.getEmail());

        notificationService.notifyFeedbackReceived(saved);

        return FeedbackResponse.from(saved);
    }

    // ============================================================
    // Patient portal submission (ENRICHED with new expansion fields)
    // ============================================================
    @Transactional
    public FeedbackResponse submitPortalFeedback(PortalFeedbackRequest request, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        boolean responseWanted = Boolean.TRUE.equals(request.getResponseWanted());

        Feedback feedback = Feedback.builder()
                .patient(patient)
                .name(patient.getFirstName() + " " + patient.getLastName())
                .email(patient.getEmail())
                .subject(request.getSubject() != null ? request.getSubject().trim() : null)
                .message(request.getMessage().trim())
                .source(FeedbackSource.PORTAL)
                .isRead(false)
                // New portal-only fields
                .type(request.getType() == null ? FeedbackType.GENERAL : request.getType())
                .rating(request.getRating())
                .responseWanted(responseWanted)
                .preferredContactMethod(request.getPreferredContactMethod())
                .status(FeedbackStatus.NEW)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Portal feedback #{} submitted by patient {} (type={}, responseWanted={})",
                saved.getId(), patient.getId(), saved.getType(), responseWanted);

        notificationService.notifyFeedbackReceived(saved);

        auditService.log(patientId, "PATIENT", "FEEDBACK_SUBMITTED",
                RESOURCE_TYPE, saved.getId(), httpRequest);

        return FeedbackResponse.from(saved);
    }

    /**
     * Backward-compatible overload — if any existing caller invokes the old
     * 1-arg version, we keep that signature working by stubbing the audit log.
     */
    @Transactional
    public FeedbackResponse submitPortalFeedback(PortalFeedbackRequest request) {
        return submitPortalFeedback(request, null);
    }

    public PageResponse<FeedbackResponse> getMyFeedback(Pageable pageable) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Page<Feedback> page = feedbackRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.from(page, FeedbackResponse::from);
    }

    // ============================================================
    // Admin operations
    // ============================================================

    /** EXISTING — kept for backward compat. */
    public PageResponse<FeedbackResponse> getAllForAdmin(Pageable pageable, Boolean unreadOnly) {
        Page<Feedback> page = (unreadOnly != null && unreadOnly)
                ? feedbackRepository.findByIsReadOrderByCreatedAtDesc(false, pageable)
                : feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.from(page, FeedbackResponse::from);
    }

    /** NEW — admin list with filters by source/status/type/isRead. */
    public PageResponse<FeedbackResponse> listForAdminFiltered(FeedbackSource source,
                                                               FeedbackStatus status,
                                                               FeedbackType type,
                                                               Boolean isRead,
                                                               Pageable pageable) {
        Page<Feedback> page = feedbackRepository.findForAdminWithFilters(source, status, type, isRead, pageable);
        return PageResponse.from(page, FeedbackResponse::from);
    }

    public FeedbackResponse getByIdForAdmin(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
        return FeedbackResponse.from(feedback);
    }

    /** NEW — admin detail view including internal notes and reviewer info. */
    public AdminFeedbackResponse getDetailForAdmin(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
        return AdminFeedbackResponse.from(feedback);
    }

    @Transactional
    public FeedbackResponse markAsRead(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        feedback.setIsRead(true);
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    @Transactional
    public FeedbackResponse toggleRead(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        feedback.setIsRead(!Boolean.TRUE.equals(feedback.getIsRead()));
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    /** NEW — portal-only status workflow update. */
    @Transactional
    public AdminFeedbackResponse updateStatus(Long id,
                                              AdminFeedbackStatusUpdateRequest request,
                                              HttpServletRequest httpRequest) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        if (feedback.getSource() != FeedbackSource.PORTAL) {
            throw new BadRequestException("Status workflow only applies to PORTAL feedback. " +
                    "Use mark-as-read / toggle-read for homepage feedback.");
        }
        if (request.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }

        Long adminId = SecurityUtils.getCurrentUserId();
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin session is invalid"));

        FeedbackStatus oldStatus = feedback.getStatus();
        feedback.setStatus(request.getStatus());
        feedback.setReviewedByAdmin(admin);
        feedback.setReviewedAt(OffsetDateTime.now());

        if (request.getAdminInternalNotes() != null) {
            feedback.setAdminInternalNotes(request.getAdminInternalNotes());
        }

        Feedback saved = feedbackRepository.save(feedback);

        auditService.log(adminId, "ADMIN", "FEEDBACK_STATUS_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} updated feedback {} status: {} -> {}",
                adminId, id, oldStatus, saved.getStatus());

        return AdminFeedbackResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feedback", "id", id);
        }
        feedbackRepository.deleteById(id);
    }

    public FeedbackStats getStats() {
        long unread = feedbackRepository.countByIsRead(false);
        long total = feedbackRepository.count();
        return FeedbackStats.builder()
                .total(total)
                .unread(unread)
                .read(total - unread)
                .build();
    }
}