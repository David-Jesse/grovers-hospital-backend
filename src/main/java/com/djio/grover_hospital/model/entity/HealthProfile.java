package com.djio.grover_hospital.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Clinical reference data for a patient. One profile per patient (1-to-1).
 *
 * Clinical fields (blood_group, genotype, allergies) are admin-managed —
 * patient submits update requests rather than editing directly.
 *
 * Non-clinical fields (emergency_contact_*, height, weight) can be edited
 * by the patient directly since they're self-reported.
 */
@Entity
@Table(name = "health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // ===== Admin-managed clinical fields =====

    /** Blood group: A+, A-, B+, B-, AB+, AB-, O+, O- */
    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    /** Genotype: AA, AS, AC, SS, SC */
    @Column(length = 5)
    private String genotype;

    /** Free-text list of known allergies — admin updates */
    @Column(columnDefinition = "TEXT")
    private String allergies;

    /** Free-text general medical notes — only visible to admin */
    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    // ===== Patient-editable self-reported fields =====

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private java.math.BigDecimal weightKg;

    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship", length = 100)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}