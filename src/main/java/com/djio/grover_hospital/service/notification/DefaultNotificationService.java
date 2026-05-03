package com.djio.grover_hospital.service.notification;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.service.notification.channel.EmailMessage;
import com.djio.grover_hospital.service.notification.channel.SmsMessage;
import com.djio.grover_hospital.service.notification.channel.WhatsappMessage;
import com.djio.grover_hospital.service.notification.sender.EmailSender;
import com.djio.grover_hospital.service.notification.sender.SmsSender;
import com.djio.grover_hospital.service.notification.sender.WhatsappSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Orchestrates which channels each business event is sent through.
 * Each `notify*` method:
 *   1. Builds channel-appropriate content (email is verbose, SMS is short, etc.)
 *   2. Dispatches to the senders for the channels enabled for that event

 * All sends are @Async so a slow provider never blocks a user request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationService implements NotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final WhatsappSender whatsappSender;

    @Value("${app.hospital.name:Grover's Hospital}")
    private String hospitalName;

    @Value("${app.hospital.notification-email}")
    private String hospitalEmail;

    @Value("${app.hospital.notification-phone:}")
    private String hospitalPhone;

    @Value("${app.hospital.contact-phone:+234 ___ ___ ____}")
    private String contactPhone;

    // Per-event channel toggles — let admin disable specific channels in properties without code changes
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

        if (bookingConfirmEmail) {
            sendSafely("booking-confirm-email", () -> emailSender.send(EmailMessage.builder()
                    .to(patient.getEmail())
                    .subject("Booking Received — #" + booking.getId())
                    .textBody(buildBookingConfirmationEmailBody(booking))
                    .build()));
        }

        if (bookingConfirmSms && hasPhone(patient)) {
            sendSafely("booking-confirm-sms", () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .text(buildBookingConfirmationSmsText(booking))
                    .build()));
        }

        if (bookingConfirmWhatsapp && hasPhone(patient)) {
            sendSafely("booking-confirm-whatsapp", () -> whatsappSender.send(WhatsappMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .templateName("booking_received")
                    .templateParams(List.of(
                            patient.getFirstName(),
                            String.valueOf(booking.getId()),
                            booking.getPreferredDate().format(DATE_FMT)
                    ))
                    .text(buildBookingConfirmationSmsText(booking))
                    .build()));
        }
    }

    // ===== Booking alert to hospital =====

    @Override
    @Async
    public void notifyBookingAlertToHospital(Booking booking) {
        Patient patient = booking.getPatient();

        sendSafely("booking-alert-email", () -> emailSender.send(EmailMessage.builder()
                .to(hospitalEmail)
                .subject("New Booking — #" + booking.getId() + " — " + patient.getFirstName() + " " + patient.getLastName())
                .textBody(buildBookingAlertEmailBody(booking))
                .build()));

        // Optional: also SMS the hospital line if configured
        if (hospitalPhone != null && !hospitalPhone.isBlank()) {
            sendSafely("booking-alert-sms", () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(hospitalPhone)
                    .text("New booking #" + booking.getId() + " from " +
                            patient.getFirstName() + " " + patient.getLastName() +
                            " for " + booking.getPreferredDate().format(DATE_FMT) + ".")
                    .build()));
        }
    }

    // ===== Booking status update to patient =====

    @Override
    @Async
    public void notifyBookingStatusUpdateToPatient(Booking booking) {
        Patient patient = booking.getPatient();

        if (statusUpdateEmail) {
            sendSafely("booking-status-email", () -> emailSender.send(EmailMessage.builder()
                    .to(patient.getEmail())
                    .subject("Booking Update — #" + booking.getId() + " — " + booking.getStatus().name())
                    .textBody(buildStatusUpdateEmailBody(booking))
                    .build()));
        }

        if (statusUpdateSms && hasPhone(patient)) {
            sendSafely("booking-status-sms", () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .text(buildStatusUpdateShortText(booking))
                    .build()));
        }

        if (statusUpdateWhatsapp && hasPhone(patient)) {
            sendSafely("booking-status-whatsapp", () -> whatsappSender.send(WhatsappMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .templateName("booking_status_update")
                    .templateParams(List.of(
                            patient.getFirstName(),
                            String.valueOf(booking.getId()),
                            booking.getStatus().name()
                    ))
                    .text(buildStatusUpdateShortText(booking))
                    .build()));
        }
    }

    // ===== Result ready notification =====

    @Async
    @Override
    public void notifyResultReady(Patient patient, String resultTitle) {
        if (resultReadyEmail) {
            sendSafely("result-ready-email", () -> emailSender.send(EmailMessage.builder()
                    .to(patient.getEmail())
                    .subject("Your medical result is ready")
                    .textBody(buildResultReadyEmailBody(patient, resultTitle))
                    .build()));
        }

        if (resultReadySms && hasPhone(patient)) {
            sendSafely("result-ready-sms", () -> smsSender.send(SmsMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .text("Hi " + patient.getFirstName() + ", your result is ready. Log in to your portal to view it. — " + hospitalName)
                    .build()));
        }

        if (resultReadyWhatsapp && hasPhone(patient)) {
            sendSafely("result-ready-whatsapp", () -> whatsappSender.send(WhatsappMessage.builder()
                    .toPhoneNumber(patient.getPhone())
                    .templateName("result_ready")
                    .templateParams(List.of(patient.getFirstName(), resultTitle))
                    .text("Hi " + patient.getFirstName() + ", your result \"" + resultTitle + "\" is ready in your portal.")
                    .build()));
        }
    }

    // ===== Password reset (email only — security best practice) =====

    @Async
    @Override
    public void notifyPasswordResetLink(Patient patient, String resetToken) {
        sendSafely("password-reset-email", () -> emailSender.send(EmailMessage.builder()
                .to(patient.getEmail())
                .subject("Reset your password")
                .textBody("""
                        Hello %s,

                        A password reset was requested for your account. Use the link below
                        to set a new password. This link expires in 30 minutes.

                        Reset token: %s

                        If you didn't request this, you can safely ignore this email.

                        — %s
                        """.formatted(patient.getFirstName(), resetToken, hospitalName))
                .build()));
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

    private String buildStatusUpdateShortText(Booking booking) {
        return "Hi " + booking.getPatient().getFirstName() + ", your booking #" + booking.getId() +
                " is now " + booking.getStatus().name() + ". — " + hospitalName;
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

    /** Wraps a send call so that a single channel failure never bubbles up to the caller. */
    private void sendSafely(String label, Runnable sendAction) {
        try {
            sendAction.run();
        } catch (Exception ex) {
            log.error("Notification dispatch failed [{}]: {}", label, ex.getMessage(), ex);
        }
    }
}