package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.sender.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportEmailService {

    private static final DateTimeFormatter EXPIRY_FMT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm 'UTC'");

    private final EmailSender emailSender;

    @Value("${app.hospital.name:Grover's Hospital}")
    private String hospitalName;


    public void sendExportReadyEmail(Patient patient, String downloadUrl, OffsetDateTime expiresAt) {
        String body = """
                Hello %s,

                Your personal data export is ready. Use the link below to download
                a JSON file containing all the information we hold about you.

                Download link:
                %s

                This link will expire on %s. After it expires, you can request a new
                export from the Account Settings page in your patient portal.

                If you did not request this export, you can safely ignore this email —
                no action is required and the link will expire on its own.

                — %s
                """.formatted(
                patient.getFirstName(),
                downloadUrl,
                expiresAt.format(EXPIRY_FMT),
                hospitalName);

        try {
            emailSender.send(EmailMessage.builder()
                    .to(patient.getEmail())
                    .subject("Your data export is ready")
                    .textBody(body)
                    .build());
            log.info("Sent data export email to patient {} ({})", patient.getId(), patient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send data export email to patient {}: {}",
                    patient.getId(), e.getMessage(), e);
            throw e;  // let DataExportService mark the job FAILED
        }
    }
}