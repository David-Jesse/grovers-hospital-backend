package com.djio.grover_hospital.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * A single encrypted file attached to a Result.
 * Each file has its own data encryption key (DEK) which is itself encrypted
 * with the master key (envelope encryption).
 */
@Entity
@Table(name = "results_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    /**
     * Original file name as uploaded (for display + download header)
     */
    @Column(name = "original_file_name", nullable = false, length = 300)
    private String originalFileName;

    /**
     * Internal storage name (UUID-based, on disk)
     */
    @Column(name = "stored_file_name", nullable = false, length = 100)
    private String storedFileName;

    /**
     * MIME type, e.g. application/pdf, image/jpeg, image/png
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * Encrypted file size on disk (bytes) — includes IV + ciphertext + auth tag
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * The data encryption key (DEK) for this file, itself encrypted using the
     * master key (envelope encryption). Stored as base64.
     * Format: [12-byte IV][encrypted DEK][16-byte auth tag], base64-encoded.
     */
    @Column(name = "encrypted_dek", nullable = false, length = 500)
    private String encryptedDek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}