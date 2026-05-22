package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.response.DataExportJobResponse;
import com.djio.grover_hospital.model.entity.DataExportJob;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.DataExportStatus;
import com.djio.grover_hospital.repository.DataExportRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates a JSON dump of all the patient's data and emails them a download link.
 *
 * The job is asynchronous: the POST returns immediately with a job id and PENDING
 * status. A separate @Async method does the JSON building and email send.
 *
 * Token is single-use-ish (status flips to PROCESSED after download). Expires after 7 days.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private static final String RESOURCE_TYPE = "DATA_EXPORT";
    private static final int EXPIRY_DAYS = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DataExportRepository jobRepository;
    private final PatientRepository patientRepository;
    private final com.djio.grover_hospital.util.DataExportCollector collector;
    private final DataExportEmailService emailService;
    private final AuditService auditService;

    @Value("${app.data-export.upload-dir:./uploads/exports}")
    private String uploadDir;

    @Value("${app.data-export.download-base-url:http://localhost:8080/api/v1/portal/account/export-data}")
    private String downloadBaseUrl;

    // ============================================================
    // Patient request — synchronous part
    // ============================================================

    @Transactional
    public DataExportJobResponse requestExport(HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        String token = generateToken();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(EXPIRY_DAYS);

        DataExportJob job = DataExportJob.builder()
                .patient(patient)
                .token(token)
                .status(DataExportStatus.PENDING)
                .expiresAt(expiresAt)
                .build();

        DataExportJob saved = jobRepository.save(job);

        auditService.log(patientId, "PATIENT", "DATA_EXPORT_REQUESTED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Patient {} requested data export, job {}", patientId, saved.getId());

        // Trigger async generation. The async method runs in a separate
        // transaction so the response returns immediately.
        generateAndEmailAsync(saved.getId());

        return DataExportJobResponse.from(saved);
    }

    // ============================================================
    // Async generation + email
    // ============================================================

    @Async
    @Transactional
    public void generateAndEmailAsync(Long jobId) {
        DataExportJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Data export job {} disappeared before processing", jobId);
            return;
        }

        Long patientId = job.getPatient().getId();
        log.info("Generating data export for patient {} (job {})", patientId, jobId);

        try {
            // Build the JSON payload — read everything the patient has
            Map<String, Object> dump = collector.collectAllDataFor(patientId);

            // Serialize
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            byte[] jsonBytes = mapper.writeValueAsBytes(dump);

            // Write to disk
            Path dir = Paths.get(uploadDir, String.valueOf(patientId));
            Files.createDirectories(dir);
            String filename = job.getToken() + ".json";
            Path filePath = dir.resolve(filename);
            Files.write(filePath, jsonBytes);

            // Update job record
            job.setFilePath(filePath.toAbsolutePath().toString());
            job.setFileSizeBytes((long) jsonBytes.length);
            job.setStatus(DataExportStatus.COMPLETED);
            job.setCompletedAt(OffsetDateTime.now());
            jobRepository.save(job);

            // Email the link
            String downloadUrl = downloadBaseUrl + "/" + job.getToken();
            emailService.sendExportReadyEmail(job.getPatient(), downloadUrl, job.getExpiresAt());

            log.info("Data export job {} completed ({} bytes, emailed to {})",
                    jobId, jsonBytes.length, job.getPatient().getEmail());

        } catch (Exception e) {
            log.error("Data export job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(DataExportStatus.FAILED);
            job.setErrorMessage(truncateError(e.getMessage()));
            jobRepository.save(job);
        }
    }

    // ============================================================
    // Token download — called by public endpoint (no JWT)
    // ============================================================

    @Transactional
    public ExportFileHandle resolveForDownload(String token) {
        DataExportJob job = jobRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid download token"));

        if (job.getStatus() == DataExportStatus.EXPIRED
                || job.getExpiresAt().isBefore(OffsetDateTime.now())) {
            if (job.getStatus() != DataExportStatus.EXPIRED) {
                job.setStatus(DataExportStatus.EXPIRED);
                jobRepository.save(job);
            }
            throw new BadRequestException("Download link has expired");
        }

        if (job.getStatus() == DataExportStatus.FAILED) {
            throw new BadRequestException("Export generation failed: " + job.getErrorMessage());
        }

        if (job.getStatus() == DataExportStatus.PENDING) {
            throw new BadRequestException("Export is still being generated. Try again in a moment.");
        }

        if (job.getFilePath() == null) {
            throw new BadRequestException("Export file is missing");
        }

        Path filePath = Paths.get(job.getFilePath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Export file no longer exists on disk");
        }

        // Mark first-download timestamp (not used to invalidate; just record)
        if (job.getDownloadedAt() == null) {
            job.setDownloadedAt(OffsetDateTime.now());
            jobRepository.save(job);
        }

        return new ExportFileHandle(filePath, "grovers_hospital_data_export.json");
    }

    // ============================================================
    // Helpers
    // ============================================================

    private String generateToken() {
        byte[] bytes = new byte[36];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String truncateError(String msg) {
        if (msg == null) return null;
        return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
    }

    /** Bundle of resolved file path + suggested download filename. */
    public record ExportFileHandle(Path path, String filename) {}
}