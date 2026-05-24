package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.model.enums.UploaderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Builder
@Entity
@Table(name = "medical_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient this document belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Set when a patient uploaded it. Null for Admin uploads
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_patient_id")
    private Patient uploadedByPatient;

    /**
     * Set when the admin uploaded it. Null for patient uploads
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_admin_id")
    private Admin uploadedByAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploaded_type", nullable = false, length = 10)
    private UploaderType uploaderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private DocumentCategory category = DocumentCategory.OTHER;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ==== Encrypted file metadata (mirrors ResultFile) =====
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Envelope-encrypted DEK, base64, Format [IV][Encrypted DEK][tag].
     */
    @Column(name = "encrypted_dek", nullable = false, length = 500)
    private String encryptedDek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}