package com.djio.grover_hospital.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * A row of the package inclusion matrix (e.g. "CBC", "ECG", "X-Ray").
 * Package-scoped — labels are not shared across packages; each package keeps its own list.
 */
@Entity
@Table(name = "package_inclusions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageInclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private HealthPackage healthPackage;

    @Column(nullable = false, length = 255)
    private String label;

    /** Optional longer description for tooltip-style detail. */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}