package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.PortalFeedbackRequest;
import com.djio.grover_hospital.model.dto.request.PublicFeedbackRequest;
import com.djio.grover_hospital.model.dto.response.FeedbackResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackStats;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.repository.FeedbackRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;

    // ==== Public homepage submission ====
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

        // Async notification to hospital
        notificationService.notifyFeedbackReceived(saved);

        return FeedbackResponse.from(saved);
    }

    // ==== Patient portal submission ====

    @Transactional
    public FeedbackResponse submitPortalFeedback(PortalFeedbackRequest request) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        Feedback feedback = Feedback.builder()
                .patient(patient)
                .name(patient.getFirstName() + " " + patient.getLastName())
                .email(patient.getEmail())
                .subject(request.getSubject() != null ? request.getSubject().trim() : null)
                .message(request.getMessage().trim())
                .source(FeedbackSource.PORTAL)
                .isRead(false)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Portal feedback #{} submitted by patient {}", saved.getId(), patient.getId());

        notificationService.notifyFeedbackReceived(saved);

        return FeedbackResponse.from(saved);
    }

    public PageResponse<FeedbackResponse> getMyFeedback(Pageable pageable) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Page<Feedback> page = feedbackRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.from(page, FeedbackResponse::from);
    }

    // ==== Admin operations ====

    public PageResponse<FeedbackResponse> getAllForAdmin(Pageable pageable, Boolean unreadOnly) {
        Page<Feedback> page = (unreadOnly != null && unreadOnly)
                ? feedbackRepository.findByIsReadOrderByCreatedAtDesc(false, pageable)
                : feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.from(page, FeedbackResponse::from);
    }

    public FeedbackResponse getByIdForAdmin(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
        return FeedbackResponse.from(feedback);
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
