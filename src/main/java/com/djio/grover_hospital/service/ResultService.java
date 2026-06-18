package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.FileStorageException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.CreateResultRequest;
import com.djio.grover_hospital.model.dto.response.AdminResultResponse;
import com.djio.grover_hospital.model.dto.response.DecryptedFileStream;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.dto.response.ResultResponse;
import com.djio.grover_hospital.model.entity.*;
import com.djio.grover_hospital.model.enums.ResultStatus;
import com.djio.grover_hospital.repository.*;
import com.djio.grover_hospital.security.JwtTokenProvider;
import com.djio.grover_hospital.security.ResultDownloadToken;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.notification.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.NoSuchAdviceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ResultService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final ResultRepository resultRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final BookingRepository bookingRepository;
    private final EncryptionService encryptionService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.result-storage.upload-dir:./uploads/results}")
    private String uploadDir;

    @Value("${app.result-download.base-url}")
    private String resultDownloadBaseUrl;

    private Path uploadRoot;

    @PostConstruct
    public void init() {
        try {
            this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadRoot);
            log.info("Result file storage directory: {}", uploadRoot);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create result upload directory: " + uploadDir, ex);
        }
    }

    // ==== Admin Upload

    @Transactional
    public AdminResultResponse uploadResult(CreateResultRequest request,
                                            List<MultipartFile> files,
                                            HttpServletRequest httpRequest
    ) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file must be provided");
        }
        for (MultipartFile file : files) {
            validateUpload(file);
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        Long adminId = SecurityUtils.getCurrentUserId();
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin session is invalid"));

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));
            if (!booking.getPatient().getId().equals(patient.getId())) {
                throw new BadRequestException("Booking does not belong to the specified patient");
            }
        }

        Result result = Result.builder()
                .patient(patient)
                .uploadedBy(admin)
                .booking(booking)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ResultStatus.AVAILABLE)
                .isNotified(false)
                .build();

        result = resultRepository.save(result);

        // Encrypt and store each file
        for (MultipartFile file : files) {
            ResultFile resultFile = encryptAndStoreFile(file, result);
            result.getFiles().add(resultFile);
        }

        result = resultRepository.save(result);

        log.info("Result #{} uploaded by admin {} for patient {} with {} file(s)", result.getId(), admin.getId(), patient.getId(), files.size());
        auditService.log(adminId, "ADMIN", "UPLOAD_RESULT", "RESULT", result.getId(), httpRequest);

        // Optionally notify patient now (default true)
        boolean shouldNotify = request.getNotifyPatient() == null || request.getNotifyPatient();
        if (shouldNotify) {
            notificationService.notifyResultReady(patient, result.getTitle());
            result.setIsNotified(true);
            resultRepository.save(result);
        }

        return AdminResultResponse.from(result);
    }

    @Transactional
    public AdminResultResponse notifyPatient(Long resultId, HttpServletRequest httpRequest) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", resultId));

        notificationService.notifyResultReady(result.getPatient(), result.getTitle());
        result.setIsNotified(true);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "NOTIFY_RESULT_READY", "RESULT", resultId, httpRequest);

        return AdminResultResponse.from(resultRepository.save(result));
    }

    // ==== Admin: list / view / delete ====
    public PageResponse<AdminResultResponse> getAllForAdmin(Pageable pageable) {
        Page<Result> page = resultRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.from(page, AdminResultResponse::from);
    }

    public PageResponse<AdminResultResponse> getResultsForPatient(Long patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", "id", patientId);
        }
        Page<Result> page = resultRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.from(page, AdminResultResponse::from);
    }

    public AdminResultResponse getByIdForAdmin(Long id, HttpServletRequest httpRequest) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", id));

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "VIEW_RESULT", "RESULT", id, httpRequest);

        return AdminResultResponse.from(result);
    }

    @Transactional
    public void deleteResult(Long id, HttpServletRequest httpRequest) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", id));

        // Delete encrypted files from disk first
        for (ResultFile file : result.getFiles()) {
            deleteEncryptedFileFromDisk(file.getStoredFileName());
        }

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "DELETE_RESULT", "RESULT", id, httpRequest);

        resultRepository.delete(result);
        log.info("Result #{} deleted by admin {}", id, adminId);
    }

    // === Patient: list / view ===
    public PageResponse<ResultResponse> getMyResults(Pageable pageable) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Page<Result> page = resultRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.from(page, ResultResponse::from);
    }

    public ResultResponse getMyResultById(Long id, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Result result = loadResultForPatient(id, patientId);

        auditService.log(patientId, "PATIENT", "VIEW_RESULT", "RESULT", id, httpRequest);

        return ResultResponse.from(result);
    }

    // ==== Patient or admin: download a specific file ====

    @Transactional
    public void requestResultDownloadLink(Long resultId, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Result result = loadResultForPatient(resultId, patientId);

        if (result.getFiles() == null || result.getFiles().isEmpty()) {
            throw new BadRequestException("This result has no downloadable file");
        }
        ResultFile file = result.getFiles().get(0);

        String token = jwtTokenProvider.generateResultDownloadToken(patientId, resultId, file.getId());
        String downloadUrl = resultDownloadBaseUrl + "/public/results/download?token=" + token;

        notificationService.notifyResultDownloadLink(result.getPatient(), result.getTitle(), downloadUrl);

        auditService.log(patientId, "PATIENT", "REQUEST_RESULT_DOWNLOAD_LINK",
                "RESULT_FILE", file.getId(), httpRequest);
    }

    @Transactional
    public DecryptedFileStream downloadByToken(String token, HttpServletRequest httpRequest) {
        ResultDownloadToken claims;
        try {
            claims = jwtTokenProvider.parseResultDownloadToken(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BadRequestException("Download link has expired. Please request a new one.");
        } catch (Exception e) {
            throw new BadRequestException("Invalid download link.");
        }

        Result result = loadResultForPatient(claims.resultId(), claims.patientId());
        ResultFile file = findFileInResult(result, claims.fileId());

        auditService.log(claims.patientId(), "PATIENT", "DOWNLOAD_RESULT_FILE_VIA_TOKEN",
                "RESULT_FILE", claims.fileId(), httpRequest);

        return openDecryptedStream(file);
    }

    public DecryptedFileStream downloadFileForAdmin(Long resultId, Long fileId, HttpServletRequest httpRequest) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", resultId));

        ResultFile file = findFileInResult(result, fileId);

        auditService.log(adminId, "ADMIN", "DOWNLOAD_RESULT_FILE", "RESULT_FILE", fileId, httpRequest);

        return openDecryptedStream(file);
    }

    // ===== Encryption + storage helpers ====

    private ResultFile encryptAndStoreFile(MultipartFile file, Result result) {
        SecretKey dek = encryptionService.generateDataKey();
        String encryptedDek = encryptionService.encryptDataKey(dek);

        String storedFileName = UUID.randomUUID().toString();
        Path targetPath = uploadRoot.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid file storage path");
        }

        long bytesWritten;
        try (InputStream input = file.getInputStream();
             OutputStream output = Files.newOutputStream(targetPath)) {
            bytesWritten = encryptionService.encryptStream(input, output, dek);
        } catch (IOException e) {
            throw new FileStorageException("Failed to write encrypted file: " + storedFileName, e);
        }

        return ResultFile.builder()
                .result(result)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .contentType(file.getContentType())
                .fileSize(bytesWritten)
                .encryptedDek(encryptedDek)
                .build();
    }

    /**
     * Returns a stream that decrypts the file in real time as the patient downloads it.
     * The plaintext is never written to disk. The stream is closed by the controller after sending.
     */
    private DecryptedFileStream openDecryptedStream(ResultFile file) {
        SecretKey dek = encryptionService.decryptDataKey(file.getEncryptedDek());
        Path filePath = uploadRoot.resolve(file.getStoredFileName()).normalize();

        if (!filePath.startsWith(uploadRoot) || !Files.exists(filePath)) {
            throw new FileStorageException("Encrypted file is missing on disk: " + file.getStoredFileName());
        }

        // Use a piped stream so decryption happens lazily on read by the response writer
        try {
            PipedInputStream patientStream = new PipedInputStream(8192);
            PipedOutputStream decryptedOut = new PipedOutputStream(patientStream);

            Thread decryptionThread = new Thread(() -> {
                try (InputStream encryptedIn = Files.newInputStream(filePath);
                     OutputStream out = decryptedOut) {
                    encryptionService.decryptStream(encryptedIn, out, dek);
                } catch (IOException e) {
                    log.error("Failed during streaming decryption of file {}", file.getId(), e);
                }
            }, "decrypt-result" + file.getId());
            decryptionThread.setDaemon(true);
            decryptionThread.start();

            return DecryptedFileStream.builder()
                    .originalFileName(file.getOriginalFileName())
                    .contentType(file.getContentType())
                    .contentLength(-1)
                    .stream(patientStream)
                    .build();
        } catch (IOException e) {
            throw new FileStorageException("Failed to set up decryption stream", e);
        }
    }

    private void deleteEncryptedFileFromDisk(String storedFileName) {
        try {
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (filePath.startsWith(uploadRoot)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete encrypted file {}: {}", storedFileName, e.getMessage());
        }
    }

    // ==== Authorization helpers ====

    private Result loadResultForPatient(Long resultId, Long patientId) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", resultId));

        if (!result.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("This result does not belong to you");
        }
        if (result.getStatus() != ResultStatus.AVAILABLE) {
            throw new ResourceNotFoundException("Result", "id", resultId);
        }

        return result;
    }

    private ResultFile findFileInResult(Result result, Long fileId) {
        return result.getFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Result file", "id", fileId));
    }

    // ==== Validation ====

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File '" + file.getOriginalFilename() +
                    "' exceeds the 10 MB size limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("File '" + file.getOriginalFilename() +
                    "' has unsupported type. Allowed: PDF, JPEG, PNG.");
        }
    }
}