package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.TestComponentFlag;
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
 * A single measured component within a lab result — e.g. one row of a CBC
 * like "Hemoglobin: 13.5 g/dL (ref 12-16), NORMAL".
 *
 * Hangs off a Result. Value is stored as a string so it can hold numerics
 * ("13.5"), qualitative results ("Positive"), or ranges. Flag is set
 * manually by the admin. Reference range is free text.
 */
@Entity
@Table(name = "test_components")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "value", length = 100)
    private String value;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "reference_range", length = 150)
    private String referenceRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag", length = 20)
    private TestComponentFlag flag;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.displayOrder == null) this.displayOrder = 0;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}