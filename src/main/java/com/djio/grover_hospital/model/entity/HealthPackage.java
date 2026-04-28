package com.djio.grover_hospital.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
     * Newline-separated list of what's included in the package,
     * e.g. "Full body checkup\nBlood tests\nDoctor consultation".
     * Frontend splits on \n to render as a list.
     */
    @Column(columnDefinition = "TEXT")
    private String inclusions;

    /**
     * Nullable - when null, frontend displays "Contact for quote".
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

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