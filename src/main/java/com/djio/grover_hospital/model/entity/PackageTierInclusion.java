package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.InclusionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * A single cell in the inclusion matrix: the intersection of a {@link PackageTier} and a {@link PackageInclusion}.
 *
 * <p>Invariant: {@link #note} is non-null exactly when {@link #status} == {@link InclusionStatus#CONDITIONAL}.
 * Enforced both in the service layer ({@code validateNoteForStatus}) and via a CHECK constraint at the DB level.</p>
 *
 * <p>Unique on (tier_id, inclusion_id) — one cell per tier-inclusion pair.</p>
 */
@Entity
@Table(
        name = "package_tier_inclusions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_pti_tier_inclusion",
                columnNames = {"tier_id", "inclusion_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageTierInclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private PackageTier tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inclusion_id", nullable = false)
    private PackageInclusion inclusion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InclusionStatus status;

    /** Required when status == CONDITIONAL; must be null otherwise. */
    @Column(length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}