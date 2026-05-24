package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.FileStorageException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.response.MedicalDocumentResponse;
import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.MedicalDocument;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.model.enums.UploaderType;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.MedicalDocumentRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manages general medical documents (insurance, referrals, external reports).
 * Both patients and admins can upload; patients see all of their own docs.
 *
 * Encryption mirrors the result-file flow exactly:
 *   generateDataKey -> encryptStream(file -> disk) -> encryptDataKey(store base64)
 *   download: decryptDataKey -> decryptStream(disk -> response)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalDocumentService {

    private static final String RESOURCE_TYPE = "MEDICAL_DOCUMENT";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;  // 10 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png"
    );

    private final MedicalDocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final EncryptionService encryptionService;
    private final AuditService auditService;

    @Value("${app.document-storage.upload-dir:./uploads/documents}")
    private String uploadDir;

    // ============================================================
    // Patient upload
    // ============================================================

    @Transactional
    public MedicalDocumentResponse uploadAsPatient(MultipartFile file, DocumentCategory category,
                                                   String title, String description,
                                                   HttpServletRequest httpRequest) {
        validateUpload(file);
        Long patientId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        MedicalDocument doc = encryptAndStore(file, patient, category, title, description);
        doc.setUploaderType(UploaderType.PATIENT);
        doc.setUploadedByPatient(patient);

        MedicalDocument saved = documentRepository.save(doc);
        auditService.log(patientId, "PATIENT", "MEDICAL_DOCUMENT_UPLOADED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Patient {} uploaded medical document {} ({})", patientId, saved.getId(), category);

        return MedicalDocumentResponse.from(saved);
    }

    // ============================================================
    // Admin upload (on behalf of a patient)
    // ============================================================

    @Transactional
    public MedicalDocumentResponse uploadAsAdmin(Long patientId, MultipartFile file,
                                                 DocumentCategory category, String title,
                                                 String description, HttpServletRequest httpRequest) {
        validateUpload(file);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        Long adminId = SecurityUtils.getCurrentUserId();
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin session is invalid"));

        MedicalDocument doc = encryptAndStore(file, patient, category, title, description);
        doc.setUploaderType(UploaderType.ADMIN);
        doc.setUploadedByAdmin(admin);

        MedicalDocument saved = documentRepository.save(doc);
        auditService.log(adminId, "ADMIN", "MEDICAL_DOCUMENT_UPLOADED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} uploaded medical document {} for patient {} ({})",
                adminId, saved.getId(), patientId, category);

        return MedicalDocumentResponse.from(saved);
    }

    // ============================================================
    // List
    // ============================================================

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getMyDocuments(DocumentCategory category) {
        Long patientId = SecurityUtils.getCurrentUserId();
        List<MedicalDocument> docs = (category != null)
                ? documentRepository.findByPatientIdAndCategoryOrderByCreatedAtDesc(patientId, category)
                : documentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return docs.stream().map(MedicalDocumentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getDocumentsForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return documentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(MedicalDocumentResponse::from).toList();
    }

    // ============================================================
    // Download (decrypt) — patient downloads own, admin downloads any
    // ============================================================

    /** Resolved decrypted bytes + metadata for a download response. */
    public record DocumentDownload(byte[] data, String fileName, String contentType) {}

    @Transactional(readOnly = true)
    public DocumentDownload downloadAsPatient(Long documentId) {
        Long patientId = SecurityUtils.getCurrentUserId();
        MedicalDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));

        if (doc.getPatient() == null || !doc.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You can only download your own documents");
        }
        return decrypt(doc);
    }

    @Transactional(readOnly = true)
    public DocumentDownload downloadAsAdmin(Long documentId) {
        MedicalDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));
        return decrypt(doc);
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteAsPatient(Long documentId, HttpServletRequest httpRequest) {
        Long patientId = SecurityUtils.getCurrentUserId();
        MedicalDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));

        if (doc.getPatient() == null || !doc.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You can only delete your own documents");
        }
        // A patient may only delete documents they uploaded themselves — not
        // ones an admin placed in their record.
        if (doc.getUploaderType() != UploaderType.PATIENT) {
            throw new BadRequestException("You can only delete documents you uploaded yourself");
        }

        removeFileQuietly(doc.getStoredFileName());
        documentRepository.delete(doc);
        auditService.log(patientId, "PATIENT", "MEDICAL_DOCUMENT_DELETED",
                RESOURCE_TYPE, documentId, httpRequest);
        log.info("Patient {} deleted own medical document {}", patientId, documentId);
    }

    @Transactional
    public void deleteAsAdmin(Long documentId, HttpServletRequest httpRequest) {
        MedicalDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));
        removeFileQuietly(doc.getStoredFileName());
        documentRepository.delete(doc);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "MEDICAL_DOCUMENT_DELETED",
                RESOURCE_TYPE, documentId, httpRequest);
        log.info("Admin {} deleted medical document {}", adminId, documentId);
    }

    // ============================================================
    // Internal — encrypt/store/decrypt
    // ============================================================

    private MedicalDocument encryptAndStore(MultipartFile file, Patient patient,
                                            DocumentCategory category, String title, String description) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String storedName = UUID.randomUUID().toString() + ".enc";
            Path target = dir.resolve(storedName);

            SecretKey dek = encryptionService.generateDataKey();

            long encryptedSize;
            try (InputStream in = file.getInputStream();
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
                encryptedSize = encryptionService.encryptStream(in, out, dek);
            }

            String encryptedDek = encryptionService.encryptDataKey(dek);

            return MedicalDocument.builder()
                    .patient(patient)
                    .category(category == null ? DocumentCategory.OTHER : category)
                    .title(title)
                    .description(description)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedName)
                    .contentType(file.getContentType())
                    .fileSize(encryptedSize)
                    .encryptedDek(encryptedDek)
                    .build();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store document", e);
        }
    }

    private DocumentDownload decrypt(MedicalDocument doc) {
        Path source = Paths.get(uploadDir).resolve(doc.getStoredFileName());
        if (!Files.exists(source)) {
            throw new ResourceNotFoundException("Document file is missing on disk");
        }
        try (InputStream in = Files.newInputStream(source);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            SecretKey dek = encryptionService.decryptDataKey(doc.getEncryptedDek());
            encryptionService.decryptStream(in, out, dek);
            return new DocumentDownload(out.toByteArray(), doc.getOriginalFileName(), doc.getContentType());
        } catch (IOException e) {
            throw new FileStorageException("Failed to read document", e);
        }
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File exceeds the 10MB limit");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct)) {
            throw new BadRequestException("Only PDF, JPEG, and PNG files are allowed");
        }
    }

    private void removeFileQuietly(String storedFileName) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(storedFileName));
        } catch (IOException e) {
            log.warn("Failed to delete document file {} from disk: {}", storedFileName, e.getMessage());
        }
    }
}