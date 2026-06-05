package com.djio.grover_hospital.model.entity;


import com.djio.grover_hospital.model.enums.Tone;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Nullable - when null, frontend displays "Contact for quote".
     */
    @Column(name = "target_audience", length = 500)
    private String targetAudience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "healthPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<PackageTier> tiers = new ArrayList<>();

    /**
     * Rows of the inclusion matrix (the test labels rendered down the left edge of the public page).
     * Package-scoped — see {@link PackageInclusion}.
     */
    @OneToMany(mappedBy = "healthPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<PackageInclusion> inclusions = new ArrayList<>();

    /** Heading-band visual tone on the public page. Defaults to GREEN. */
    @Enumerated(EnumType.STRING)
    @Column(name = "heading_tone", nullable = false, length = 20)
    @Builder.Default
    private Tone headingTone = Tone.GREEN;

    /** Pricing-band visual tone on the public page. Defaults to GREEN. */
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_tone", nullable = false, length = 20)
    @Builder.Default
    private Tone pricingTone = Tone.GREEN;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}