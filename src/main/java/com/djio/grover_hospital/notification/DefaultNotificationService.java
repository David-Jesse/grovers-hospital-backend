package com.djio.grover_hospital.notification;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.DeliveryChannel;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.PortalNotificationType;
import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.service.NotificationDeliveryLogService;
import com.djio.grover_hospital.service.PortalNotificationService;
import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.channel.WhatsappMessage;
import com.djio.grover_hospital.notification.sender.EmailSender;
import com.djio.grover_hospital.notification.sender.SmsSender;
import com.djio.grover_hospital.notification.sender.WhatsappSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import com.djio.grover_hospital.service.NotificationPreferenceService;
import com.djio.grover_hospital.service.NotificationPreferenceService.NotificationEvent;
import com.djio.grover_hospital.service.NotificationPreferenceService.NotificationChannel;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/**
 * Orchestrates external channels (email / SMS / WhatsApp) AND drops a record
 * into the in-portal notification table for the bell-icon UI.

 * Each notify* method does up to four things:
 *   1. Build channel-appropriate content
 *   2. Dispatch to external senders (per the channel toggle config)
 *   3. Create an in-portal Notification entry for the recipient(s)
 *   4. Wrap every channel in sendSafely so one failure doesn't poison the others
 *
 * Patient-bound notifications respect a two-layer opt-out:
 *   1. App-level toggle in application.properties (kill switch)
 *   2. Per-patient notification_preferences row (opt-out)
 * Both must allow a channel for the send to fire.
 *
 * Admin-bound notifications are NOT gated by patient preferences — admins
 * receive everything the app config allows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationService implements NotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final WhatsappSender whatsappSender;
    private final PortalNotificationService portalNotificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final NotificationDeliveryLogService deliveryLogService;

    @Value("${app.hospital.name:Grover's Hospital}")
    private String hospitalName;

    @Value("${app.hospital.notification-email}")
    private String hospitalEmail;

    @Value("${app.hospital.notification-phone:}")
    private String hospitalPhone;

    @Value("${app.hospital.contact-phone:+234 ___ ___ ____}")
    private String contactPhone;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // Channel toggles per event
    @Value("${app.notification.events.booking-confirmation.email:true}")
    private boolean bookingConfirmEmail;
    @Value("${app.notification.events.booking-confirmation.sms:false}")
    private boolean bookingConfirmSms;
    @Value("${app.notification.events.booking-confirmation.whatsapp:true}")
    private boolean bookingConfirmWhatsapp;

    @Value("${app.notification.events.booking-status-update.email:true}")
    private boolean statusUpdateEmail;
    @Value("${app.notification.events.booking-status-update.sms:true}")
    private boolean statusUpdateSms;
    @Value("${app.notification.events.booking-status-update.whatsapp:true}")
    private boolean statusUpdateWhatsapp;

    @Value("${app.notification.events.result-ready.email:true}")
    private boolean resultReadyEmail;
    @Value("${app.notification.events.result-ready.sms:true}")
    private boolean resultReadySms;
    @Value("${app.notification.events.result-ready.whatsapp:false}")
    private boolean resultReadyWhatsapp;

    // ===== Booking confirmation to patient =====

    @Override
    @Async
    public void notifyBookingConfirmationToPatient(Booking booking) {
        Patient patient = booking.getPatient();
        Long patientId = patient.getId();
        NotificationEvent event = NotificationEvent.BOOKING_CONFIRMATION;

        if (bookingConfirmEmail
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.EMAIL)) {
            sendAndLog("booking-confirm-email",
                    patientId, "BOOKING_CONFIRMATION", "BOOKING", booking.getId(),
                    DeliveryChannel.EMAIL, patient.getEmail(),
                    () -> emailSender.send(EmailMessage.builder()
                            .to(patient.getEmail())
                            .subject("Booking Received — #" + booking.getId())
                            .textBody(buildBookingConfirmationEmailBody(booking))
                            .build()));
        }

        if (bookingConfirmSms && hasPhone(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.SMS)) {
            sendAndLog("booking-confirm-sms", patientId, "BOOKING_CONFIRMATION", "BOOKING", booking.getId(),
                    DeliveryChannel.SMS, patient.getPhone(),
                    () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .text(buildBookingConfirmationSmsText(booking))
                    .build()));
        }

        if (bookingConfirmWhatsapp && hasWhatsappTarget(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.WHATSAPP)) {

            sendAndLog("booking-confirm-whatsapp", patientId, "BOOKING_CONFIRMATION",
                    "BOOKING",
                    booking.getId(), DeliveryChannel.WHATSAPP, resolveWhatsappNumber(patient),
                    () -> whatsappSender.send(WhatsappMessage.builder()
                    .toPhoneNumber(resolveWhatsappNumber(patient))
                    .templateName("booking_received")
                    .templateParams(List.of(
                            patient.getFirstName(),
                            String.valueOf(booking.getId()),
                            booking.getPreferredDate().format(DATE_FMT)
                    ))
                    .text(buildBookingConfirmationSmsText(booking))
                    .build()));
        }

        // In-portal notification — always fires (the patient explicitly opted into the portal)
        sendSafely("booking-confirm-portal", () -> portalNotificationService.createForPatient(
                patient.getId(),
                PortalNotificationType.BOOKING_RECEIVED,
                String.format("Your booking #%d for %s has been received. We'll contact you to confirm the time.",
                        booking.getId(), booking.getPreferredDate().format(DATE_FMT))));
    }

    // ===== Booking alert to hospital =====
    // NOT gated by patient preferences — admin always receives.

    @Override
    @Async
    public void notifyBookingAlertToHospital(Booking booking) {
        Patient patient = booking.getPatient();

        sendSafely("booking-alert-email", () -> emailSender.send(EmailMessage.builder()
                .to(hospitalEmail)
                .subject("New Booking — #" + booking.getId() + " — " + patient.getFirstName() + " " + patient.getLastName())
                .textBody(buildBookingAlertEmailBody(booking))
                .build()));

        if (hospitalPhone != null && !hospitalPhone.isBlank()) {
            sendSafely("booking-alert-sms", () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(hospitalPhone)
                    .text("New booking #" + booking.getId() + " from " +
                            patient.getFirstName() + " " + patient.getLastName() +
                            " for " + booking.getPreferredDate().format(DATE_FMT) + ".")
                    .build()));
        }

        // In-portal alert to all admins
        sendSafely("booking-alert-portal", () -> portalNotificationService.createForAllAdmins(
                PortalNotificationType.NEW_BOOKING_ALERT,
                String.format("New booking #%d from %s %s for %s — review in the bookings dashboard.",
                        booking.getId(),
                        patient.getFirstName(), patient.getLastName(),
                        booking.getPreferredDate().format(DATE_FMT))));
    }

    // ===== Booking status update to patient =====

    @Override
    @Async
    public void notifyBookingStatusUpdateToPatient(Booking booking) {
        Patient patient = booking.getPatient();
        Long patientId = patient.getId();
        NotificationEvent event = NotificationEvent.BOOKING_STATUS_UPDATE;

        if (statusUpdateEmail
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.EMAIL)) {
            sendAndLog("booking-status-email",
                    patientId, "BOOKING_STATUS_UPDATE", "BOOKING", booking.getId(),
                    DeliveryChannel.EMAIL, patient.getEmail(),
                    () -> emailSender.send(EmailMessage.builder()
                            .to(patient.getEmail())
                            .subject("Booking Update — #" + booking.getId() + " — " + booking.getStatus().name())
                            .textBody(buildStatusUpdateEmailBody(booking))
                            .build()));
        }

        if (statusUpdateSms && hasPhone(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.SMS)) {
            sendAndLog("booking-status-sms",
                    patientId, "BOOKING_STATUS_UPDATE", "BOOKING", booking.getId(),
                    DeliveryChannel.SMS, patient.getPhone(),
                    () -> smsSender.send(SmsMessage.builder()
                            .toPhoneNumber(patient.getPhone())
                            .text(buildStatusUpdateShortText(booking))
                            .build()));
        }

        if (statusUpdateWhatsapp && hasWhatsappTarget(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.WHATSAPP)) {
            sendAndLog("booking-status-whatsapp",
                    patientId, "BOOKING_STATUS_UPDATE", "BOOKING", booking.getId(),
                    DeliveryChannel.WHATSAPP, resolveWhatsappNumber(patient),
                    () -> whatsappSender.send(WhatsappMessage.builder()
                            .toPhoneNumber(resolveWhatsappNumber(patient))
                            .templateName("booking_status_update")
                            .templateParams(List.of(
                                    patient.getFirstName(),
                                    String.valueOf(booking.getId()),
                                    booking.getStatus().name()
                            ))
                            .text(buildStatusUpdateShortText(booking))
                            .build()));
        }

        // In-portal notification — always fires
        PortalNotificationType portalType = mapStatusToPortalType(booking.getStatus());
        sendSafely("booking-status-portal", () -> portalNotificationService.createForPatient(
                patient.getId(),
                portalType,
                buildStatusUpdatePortalMessage(booking)));
    }

    @Override
    @Async
    public void notifyAppointmentReminderToPatient(Booking booking) {
        Patient patient = booking.getPatient();
        Long patientId = patient.getId();
        // Reuse the booking-status-update preference toggles for reminders
        NotificationEvent event = NotificationEvent.BOOKING_STATUS_UPDATE;

        if (statusUpdateEmail
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.EMAIL)) {
            sendAndLog("appointment-reminder-email",
                    patientId, "APPOINTMENT_REMINDER", "BOOKING", booking.getId(),
                    DeliveryChannel.EMAIL, patient.getEmail(),
                    () -> emailSender.send(EmailMessage.builder()
                            .to(patient.getEmail())
                            .subject("Reminder: your appointment is tomorrow — #" + booking.getId())
                            .textBody(buildReminderEmailBody(booking))
                            .build()));
        }

        if (statusUpdateSms && hasPhone(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.SMS)) {
            sendAndLog("appointment-reminder-sms",
                    patientId, "APPOINTMENT_REMINDER", "BOOKING", booking.getId(),
                    DeliveryChannel.SMS, patient.getPhone(),
                    () -> smsSender.send(SmsMessage.builder()
                            .toPhoneNumber(patient.getPhone())
                            .text("Reminder: Hi " + patient.getFirstName() + ", your booking #" + booking.getId() +
                                    " is tomorrow (" + booking.getPreferredDate().format(DATE_FMT) + "). — " + hospitalName)
                            .build()));
        }

        if (statusUpdateWhatsapp && hasWhatsappTarget(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.WHATSAPP)) {
            sendAndLog("appointment-reminder-whatsapp",
                    patientId, "APPOINTMENT_REMINDER", "BOOKING", booking.getId(),
                    DeliveryChannel.WHATSAPP, resolveWhatsappNumber(patient),
                    () -> whatsappSender.send(WhatsappMessage.builder()
                            .toPhoneNumber(resolveWhatsappNumber(patient))
                            .templateName("appointment_reminder")
                            .templateParams(List.of(
                                    patient.getFirstName(),
                                    String.valueOf(booking.getId()),
                                    booking.getPreferredDate().format(DATE_FMT)
                            ))
                            .text("Reminder: Hi " + patient.getFirstName() + ", your booking #" + booking.getId() +
                                    " is tomorrow (" + booking.getPreferredDate().format(DATE_FMT) + ").")
                            .build()));
        }

        // In-portal bell notification — always fires
        sendSafely("appointment-reminder-portal", () -> portalNotificationService.createForPatient(
                patient.getId(),
                com.djio.grover_hospital.model.enums.PortalNotificationType.BOOKING_CONFIRMED,
                String.format("Reminder: your booking #%d is scheduled for tomorrow, %s.",
                        booking.getId(), booking.getPreferredDate().format(DATE_FMT))));
    }

    // ===== Result ready notification =====

    @Override
    @Async
    public void notifyResultReady(Patient patient, String resultTitle) {
        Long patientId = patient.getId();
        NotificationEvent event = NotificationEvent.RESULT_READY;

        if (resultReadyEmail
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.EMAIL)) {
            sendAndLog("result-ready-email",
                    patientId, "RESULT_READY", null, null,
                    DeliveryChannel.EMAIL, patient.getEmail(),
                    () -> emailSender.send(EmailMessage.builder()
                            .to(patient.getEmail())
                            .subject("Your medical result is ready")
                            .textBody(buildResultReadyEmailBody(patient, resultTitle))
                            .build()));
        }

        if (resultReadySms && hasPhone(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.SMS)) {
            sendAndLog("result-ready-sms",
                    patientId, "RESULT_READY", null, null,
                    DeliveryChannel.SMS, patient.getPhone(),
                    () -> smsSender.send(SmsMessage.builder()
                            .toPhoneNumber(patient.getPhone())
                            .text("Hi " + patient.getFirstName() + ", your result is ready. Log in to your portal to view it. — " + hospitalName)
                            .build()));
        }

        if (resultReadyWhatsapp && hasWhatsappTarget(patient)
                && notificationPreferenceService.shouldSend(patientId, event, NotificationChannel.WHATSAPP)) {
            sendAndLog("result-ready-whatsapp",
                    patientId, "RESULT_READY", null, null,
                    DeliveryChannel.WHATSAPP, resolveWhatsappNumber(patient),
                    () -> whatsappSender.send(WhatsappMessage.builder()
                            .toPhoneNumber(resolveWhatsappNumber(patient))
                            .templateName("result_ready")
                            .templateParams(List.of(patient.getFirstName(), resultTitle))
                            .text("Hi " + patient.getFirstName() + ", your result \"" + resultTitle + "\" is ready in your portal.")
                            .build()));
        }

        // In-portal notification — always fires
        sendSafely("result-ready-portal", () -> portalNotificationService.createForPatient(
                patient.getId(),
                PortalNotificationType.RESULT_READY,
                String.format("Your result \"%s\" is ready to view in your portal.", resultTitle)));
    }
    // ===== Password reset (email only — security best practice) =====
    // NOT gated by patient prefs — security email, must always send.

    @Override
    @Async
    public void notifyPasswordResetLink(Patient patient, String resetToken) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken;

        sendAndLog("password-reset-email",
                patient.getId(), "PASSWORD_RESET", null, null,
                DeliveryChannel.EMAIL, patient.getEmail(),
                () -> emailSender.send(EmailMessage.builder()
                        .to(patient.getEmail())
                        .subject("Reset your password")
                        .textBody("""
                            Hello %s,

                            A password reset was requested for your account. Use the link below
                            to set a new password. This link expires in 30 minutes.

                            Reset token: %s

                            If you didn't request this, you can safely ignore this email.

                            — %s
                            """.formatted(patient.getFirstName(), resetUrl, hospitalName))
                        .build()));

        // Intentionally NO in-portal notification — password reset doesn't need a UI badge,
        // and dropping the token into a notification record would weaken security.
    }

    @Override
    @Async
    public void notifyResultDownloadLink(Patient patient, String resultTitle, String downloadUrl) {
        // Always send — patient explicitly requested this download link
        sendAndLog("result-download-link",
                patient.getId(), "RESULT_DOWNLOAD_LINK", null, null,
                DeliveryChannel.EMAIL, patient.getEmail(),
                () -> emailSender.send(EmailMessage.builder()
                        .to(patient.getEmail())
                        .subject("Your result download link — " + resultTitle)
                        .textBody(buildResultDownloadEmailBody(patient, resultTitle, downloadUrl))
                        .build()));
    }

    // ===== Feedback received (admin alert) =====
    // NOT gated by patient prefs — this is for admins.

    @Override
    @Async
    public void notifyFeedbackReceived(Feedback feedback) {
        String sourceLabel = feedback.getSource() == FeedbackSource.PORTAL
                ? "Patient Portal" : "Homepage Form";

        String text = """
                New feedback received from the %s.

                From:     %s
                Email:    %s
                Subject:  %s

                Message:
                %s

                ─────────────────────────────────────
                View and respond in the admin dashboard.
                """.formatted(
                sourceLabel,
                feedback.getName() != null ? feedback.getName() : "(anonymous)",
                feedback.getEmail() != null ? feedback.getEmail() : "(no email)",
                feedback.getSubject() != null && !feedback.getSubject().isBlank()
                        ? feedback.getSubject() : "(no subject)",
                feedback.getMessage()
        );

        sendSafely("feedback-received-email", () -> emailSender.send(EmailMessage.builder()
                .to(hospitalEmail)
                .subject("New Feedback — " + sourceLabel +
                        (feedback.getSubject() != null && !feedback.getSubject().isBlank()
                                ? " — " + feedback.getSubject() : ""))
                .textBody(text)
                .build()));

        // In-portal alert to all admins
        sendSafely("feedback-received-portal", () -> portalNotificationService.createForAllAdmins(
                PortalNotificationType.NEW_FEEDBACK_ALERT,
                String.format("New feedback from %s via %s — \"%s\"",
                        feedback.getName() != null ? feedback.getName() : "anonymous",
                        sourceLabel,
                        truncate(feedback.getMessage(), 100))));
    }

    // ===== Content builders =====

    private String buildBookingConfirmationEmailBody(Booking booking) {
        Patient p = booking.getPatient();
        return """
                Hello %s,

                Thank you for booking with %s. We have received your request.

                Booking ID:      #%d
                Type:            %s
                %s
                Preferred date:  %s
                Status:          PENDING

                A member of our team will reach out to you shortly to confirm
                the time and any final details.

                If you have any questions, contact us at %s.

                — %s
                """.formatted(
                p.getFirstName(), hospitalName, booking.getId(),
                booking.getBookingType().name(), bookingTargetLine(booking),
                booking.getPreferredDate().format(DATE_FMT), contactPhone, hospitalName);
    }

    private String buildBookingConfirmationSmsText(Booking booking) {
        return "Hi " + booking.getPatient().getFirstName() + ", your booking #" + booking.getId() +
                " for " + booking.getPreferredDate().format(DATE_FMT) +
                " is received. We'll call to confirm shortly. — " + hospitalName;
    }

    private String buildBookingAlertEmailBody(Booking booking) {
        Patient p = booking.getPatient();
        return """
                A new booking has been submitted.

                Booking ID:      #%d
                Patient:         %s %s
                Patient email:   %s
                Patient phone:   %s
                Type:            %s
                %s
                Preferred date:  %s
                Patient notes:   %s

                Please log in to the admin dashboard to confirm or follow up.
                """.formatted(
                booking.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhone() != null ? p.getPhone() : "(not provided)",
                booking.getBookingType().name(), bookingTargetLine(booking),
                booking.getPreferredDate().format(DATE_FMT),
                booking.getNotes() != null && !booking.getNotes().isBlank() ? booking.getNotes() : "(none)");
    }

    private String buildResultDownloadEmailBody(Patient patient, String resultTitle, String downloadUrl) {
        return """
            Hello %s,

            Here's the secure download link for your result: "%s"

            %s

            For your security, this link expires in 30 minutes. If you didn't request this,
            you can safely ignore this email.

            — %s
            """.formatted(patient.getFirstName(), resultTitle, downloadUrl, hospitalName);
    }

    private String buildStatusUpdateEmailBody(Booking booking) {
        Patient p = booking.getPatient();
        String statusMessage = switch (booking.getStatus()) {
            case CONFIRMED -> "Your booking has been confirmed. We look forward to seeing you.";
            case CANCELLED -> "Your booking has been cancelled. If this was unexpected, please contact us.";
            case COMPLETED -> "Your appointment has been marked as completed. Thank you for visiting us.";
            default -> "There has been an update to your booking.";
        };
        return """
                Hello %s,

                %s

                Booking ID:      #%d
                %s
                Preferred date:  %s
                Status:          %s

                For any questions, contact us at %s.

                — %s
                """.formatted(
                p.getFirstName(), statusMessage, booking.getId(),
                bookingTargetLine(booking), booking.getPreferredDate().format(DATE_FMT),
                booking.getStatus().name(), contactPhone, hospitalName);
    }

    private String buildReminderEmailBody(Booking booking) {
        Patient p = booking.getPatient();
        return """
                Hello %s,
 
                This is a friendly reminder that you have an appointment scheduled
                for tomorrow.
 
                Booking ID:      #%d
                %s
                Date:            %s
 
                If you need to reschedule, you can do so from your patient portal,
                or contact us at %s.
 
                We look forward to seeing you.
 
                — %s
                """.formatted(
                p.getFirstName(),
                booking.getId(),
                bookingTargetLine(booking),
                booking.getPreferredDate().format(DATE_FMT),
                contactPhone,
                hospitalName);
    }

    private String buildStatusUpdateShortText(Booking booking) {
        return "Hi " + booking.getPatient().getFirstName() + ", your booking #" + booking.getId() +
                " is now " + booking.getStatus().name() + ". — " + hospitalName;
    }

    private String buildStatusUpdatePortalMessage(Booking booking) {
        return switch (booking.getStatus()) {
            case CONFIRMED -> String.format("Your booking #%d has been confirmed for %s.",
                    booking.getId(), booking.getPreferredDate().format(DATE_FMT));
            case CANCELLED -> String.format("Your booking #%d has been cancelled.", booking.getId());
            case COMPLETED -> String.format("Your booking #%d has been marked as completed.", booking.getId());
            default -> String.format("Your booking #%d has been updated.", booking.getId());
        };
    }

    private PortalNotificationType mapStatusToPortalType(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> PortalNotificationType.BOOKING_CONFIRMED;
            case CANCELLED -> PortalNotificationType.BOOKING_CANCELLED;
            default -> PortalNotificationType.BOOKING_RECEIVED;
        };
    }

    private String buildResultReadyEmailBody(Patient patient, String resultTitle) {
        return """
                Hello %s,

                Your medical result is now available in your patient portal:

                  "%s"

                Please log in to view it. For questions about your result,
                contact us at %s.

                — %s
                """.formatted(patient.getFirstName(), resultTitle, contactPhone, hospitalName);
    }

    private String bookingTargetLine(Booking booking) {
        return switch (booking.getBookingType()) {
            case CONSULTATION -> "Department:      " +
                    (booking.getDepartment() != null ? booking.getDepartment().getName() : "(not specified)");
            case PACKAGE -> {
                String pkg = booking.getHealthPackage() != null ? booking.getHealthPackage().getName() : "(not specified)";
                String tier = booking.getPackageTier() != null ? booking.getPackageTier().getName() : null;
                yield "Package:         " + pkg + (tier != null ? " (" + tier + ")" : "");
            }
        };
    }

    // ===== Helpers =====

    private boolean hasPhone(Patient patient) {
        return patient.getPhone() != null && !patient.getPhone().isBlank();
    }

    /**
     * True if we have any number we can send a WhatsApp message to —
     * either a dedicated WhatsApp number or a fallback to the SMS phone.
     */
    private boolean hasWhatsappTarget(Patient patient) {
        return (patient.getWhatsappNumber() != null && !patient.getWhatsappNumber().isBlank())
                || hasPhone(patient);
    }

    /**
     * Prefer the dedicated WhatsApp number; fall back to the SMS phone.
     * Caller should gate on hasWhatsappTarget first.
     */
    private String resolveWhatsappNumber(Patient patient) {
        if (patient.getWhatsappNumber() != null && !patient.getWhatsappNumber().isBlank()) {
            return patient.getWhatsappNumber();
        }
        return patient.getPhone();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    /** Wraps a send call so that a single channel failure never bubbles up to the caller. */
    private void sendSafely(String label, Runnable sendAction) {
        try {
            sendAction.run();
        } catch (Exception ex) {
            log.error("Notification dispatch failed [{}]: {}", label, ex.getMessage(), ex);
        }
    }

    /**
     * Patient-bound version of sendSafely that captures the SendResult and writes it to
     * notification_delivery_logs. Failures (exceptions OR provider-reported failures) get
     * logged with status=FAILED. Never throws — channel failures stay isolated.
     */
    private void sendAndLog(String label,
                            Long patientId,
                            String eventName,
                            String referenceType,
                            Long referenceId,
                            DeliveryChannel channel,
                            String recipient,
                            Supplier<SendResult> sendAction) {
        SendResult result;
        try {
            result = sendAction.get();
        } catch (Exception ex) {
            log.error("Notification dispatch failed [{}]: {}", label, ex.getMessage(), ex);
            result = SendResult.failure("Exception: " + ex.getMessage());
        }
        try {
            deliveryLogService.record(patientId, eventName, referenceType, referenceId,
                    channel, recipient, result);
        } catch (Exception logEx) {
            log.error("Failed to record delivery log for [{}]: {}", label, logEx.getMessage());
        }
    }
}