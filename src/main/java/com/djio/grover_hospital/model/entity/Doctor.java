package com.djio.grover_hospital.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Hospital doctor / consultant. Used as an optional FK from records like
 * medications, chronic conditions, visits, etc. so we can link clinical data
 * to real practitioners
 * <p>
 * Free-text fields like prescribed_by_text on related entities provide a
 * fallback when the doctor isn't in the system
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /**
     * Optional professional title
     */
    @Column(length = 20)
    private String title;

    /**
     * Speciality
     */
    @Column(length = 200)
    private String specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

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
