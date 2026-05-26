package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.ProfileUpdateField;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * A patient's request to change an admin-only health-profile field.
 * Admin reviews and either APPROVES (which applies the change to the
 * HealthProfile) or REJECTS.
 *
 * For target_field = OTHER there's nothing to auto-apply — approval just
 * marks it handled; the admin makes the actual change manually.
 */
@Entity
@Table(name = "profile_update_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_field", nullable = false, length = 20)
    private ProfileUpdateField targetField;

    /** Only used when targetField = OTHER. */
    @Column(name = "other_field_description", length = 200)
    private String otherFieldDescription;

    /** Snapshot of the field's value when the request was made. */
    @Column(name = "current_value", columnDefinition = "TEXT")
    private String currentValue;

    @Column(name = "proposed_value", nullable = false, columnDefinition = "TEXT")
    private String proposedValue;

    @Column(name = "patient_note", columnDefinition = "TEXT")
    private String patientNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProfileUpdateStatus status = ProfileUpdateStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private Admin reviewedByAdmin;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = ProfileUpdateStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}