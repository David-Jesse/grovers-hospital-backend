package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.model.enums.UploaderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * A general medical document uploaded by a patient or an admin — insurance
 * cards, external reports, referrals, etc. Distinct from ResultFile, which is
 * specifically a lab-result attachment.
 *
 * Encrypted at rest with the same AES-256-GCM envelope scheme as result files:
 * each document has its own DEK, itself encrypted with the master key.
 */
@Entity
@Table(name = "medical_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The patient this document belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** Set when a patient uploaded it. Null for admin uploads. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_patient_id")
    private Patient uploadedByPatient;

    /** Set when an admin uploaded it. Null for patient uploads. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_admin_id")
    private Admin uploadedByAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploader_type", nullable = false, length = 10)
    private UploaderType uploaderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private DocumentCategory category = DocumentCategory.OTHER;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ===== Encrypted file metadata (mirrors ResultFile) =====

    @Column(name = "original_file_name", nullable = false, length = 300)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 100)
    private String storedFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /** Encrypted file size on disk (bytes) — IV + ciphertext + auth tag. */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** Envelope-encrypted DEK, base64. Format [IV][encrypted DEK][tag]. */
    @Column(name = "encrypted_dek", nullable = false, length = 500)
    private String encryptedDek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}