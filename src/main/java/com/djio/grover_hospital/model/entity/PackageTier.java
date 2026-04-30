package com.djio.grover_hospital.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "package_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private HealthPackage healthPackage;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Newline-separated list of what's included in this tier,
     * e.g. "Hepatitis B\nHIV Screening\nX-ray (Chest)\nUrinalysis".
     * Frontend splits on \n to render as a list.
     */

    @Column(columnDefinition =  "TEXT")
    private String inclusions;

    /** Price for male patients. Nullable when the package has no fixed price*/
    @Column(name = "price_male", precision = 12, scale = 2)
    private BigDecimal priceMale;

    /** Price for female patients. Nullable when the package has no fixed price */
    @Column(name = "price_female", precision = 12, scale = 2)
    private BigDecimal priceFemale;

    /** Optional notes specific to this tier, e.g. "7% bundle discount applied". */
    @Column(length = 500)
    private String notes;

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