package com.djio.grover_hospital.service;

import com.djio.grover_hospital.config.NotificationDefaultProperties;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.NotificationPreferencesRequest;
import com.djio.grover_hospital.model.dto.response.NotificationPreferencesResponse;
import com.djio.grover_hospital.model.entity.NotificationPreference;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.repository.NotificationPreferenceRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private static final String RESOURCE_TYPE = "NOTIFICATION_PREFERENCES";

    private final NotificationPreferenceRepository repository;
    private final PatientRepository patientRepository;
    private final NotificationDefaultProperties defaults;
    private final AuditService auditService;

    // Patient-facing

    @Transactional
    public NotificationPreferencesResponse getMyPreferences() {
        Long patientId = SecurityUtils.getCurrentUserId();
        NotificationPreference prefs = repository.findByPatientId(patientId)
                .orElseGet(() -> createDefaultsFor(patientId));
        return NotificationPreferencesResponse.from(prefs);
    }

    @Transactional
    public NotificationPreferencesResponse updateMyPreferences(NotificationPreferencesRequest request, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        NotificationPreference prefs = repository.findByPatientId(patientId)
                .orElseGet(() -> createDefaultsFor(patientId));

        if (request.getBookingConfirmationEmail() != null)
            prefs.setBookingConfirmationEmail(request.getBookingConfirmationEmail());
        if (request.getBookingConfirmationSms() != null)
            prefs.setBookingConfirmationSms(request.getBookingConfirmationSms());
        if (request.getBookingConfirmationWhatsapp() != null)
            prefs.setBookingConfirmationWhatsapp(request.getBookingConfirmationWhatsapp());

        if (request.getBookingStatusUpdateEmail() != null)
            prefs.setBookingStatusUpdateEmail(request.getBookingStatusUpdateEmail());
        if (request.getBookingStatusUpdateSms() != null)
            prefs.setBookingStatusUpdateSms(request.getBookingStatusUpdateSms());
        if (request.getBookingStatusUpdateWhatsapp() != null)
            prefs.setBookingStatusUpdateWhatsapp(request.getBookingStatusUpdateWhatsapp());

        if (request.getResultReadyEmail() != null)
            prefs.setResultReadyEmail(request.getResultReadyEmail());
        if (request.getResultReadySms() != null)
            prefs.setResultReadySms(request.getResultReadySms());
        if (request.getResultReadyWhatsapp() != null)
            prefs.setResultReadyWhatsapp(request.getResultReadyWhatsapp());

        NotificationPreference saved = repository.save(prefs);

        auditService.log(patientId, "PATIENT", "NOTIFICATION_PREFS_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        log.info("Patient {] updated notification preferences", patientId);

        return NotificationPreferencesResponse.from(saved);
    }

    public enum NotificationEvent {
        BOOKING_CONFIRMATION,
        BOOKING_STATUS_UPDATE,
        RESULT_READY
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        WHATSAPP
    }

    /**
     * Returns whether the given patient has opted in to receive
     * (event, channel). If the patient has no prefs row yet, lazily
     * creates one using app-level defaults, then answers from that.
     * <p>
     * This is the per-patient opt-out layer. The caller (DefaultNotificationService)
     * is responsible for also checking the App-level kill switch in application.properties BEFORE calling this
     */
    @Transactional
    public boolean shouldSend(Long patientId, NotificationEvent event, NotificationChannel channel) {
        NotificationPreference prefs = repository.findByPatientId(patientId)
                .orElseGet(() -> createDefaultsFor(patientId));

        return switch (event) {
            case BOOKING_CONFIRMATION -> switch (channel) {
                case EMAIL -> Boolean.TRUE.equals(prefs.getBookingConfirmationEmail());
                case SMS -> Boolean.TRUE.equals(prefs.getBookingConfirmationEmail());
                case WHATSAPP -> Boolean.TRUE.equals(prefs.getBookingConfirmationWhatsapp());
            };
            case BOOKING_STATUS_UPDATE -> switch (channel) {
                case EMAIL -> Boolean.TRUE.equals(prefs.getBookingStatusUpdateEmail());
                case SMS -> Boolean.TRUE.equals(prefs.getBookingStatusUpdateSms());
                case WHATSAPP -> Boolean.TRUE.equals(prefs.getBookingStatusUpdateWhatsapp());
            };
            case RESULT_READY -> switch (channel) {
                case EMAIL -> Boolean.TRUE.equals(prefs.getResultReadyEmail());
                case SMS -> Boolean.TRUE.equals(prefs.getResultReadySms());
                case WHATSAPP -> Boolean.TRUE.equals(prefs.getResultReadyWhatsapp());
            };
        };
    }

    // ======= Internal =======

    private NotificationPreference createDefaultsFor(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        NotificationPreference prefs = NotificationPreference.builder()
                .patient(patient)
                .bookingConfirmationEmail(defaults.getBookingConfirmation().isEmail())
                .bookingConfirmationSms(defaults.getBookingConfirmation().isSms())
                .bookingConfirmationWhatsapp(defaults.getBookingConfirmation().isWhatsapp())
                .bookingStatusUpdateEmail(defaults.getBookingStatusUpdate().isEmail())
                .bookingStatusUpdateSms(defaults.getBookingStatusUpdate().isSms())
                .bookingStatusUpdateWhatsapp(defaults.getBookingStatusUpdate().isWhatsapp())
                .resultReadyEmail(defaults.getResultReady().isEmail())
                .resultReadySms(defaults.getResultReady().isSms())
                .resultReadyWhatsapp(defaults.getResultReady().isWhatsapp())
                .build();

        NotificationPreference saved = repository.save(prefs);
        log.info("Auto created notification preferences for patient {} from app defaults (id={}", patientId, saved.getId());
        return saved;
    }
}
