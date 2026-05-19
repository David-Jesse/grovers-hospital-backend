package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.ConditionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "chronic_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChronicCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ConditionStatus status = ConditionStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Optional Doctor FK pattern
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managing_doctor_id")
    private Doctor managingDoctor;

    @Column(name = "managing_doctor_text", length = 200)
    private String managingDoctorText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ConditionStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}