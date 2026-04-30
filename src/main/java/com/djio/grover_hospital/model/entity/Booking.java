package com.djio.grover_hospital.model.entity;


import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_patient", columnList = "patient_id"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_preferred_date", columnList = "preferred_date")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    private BookingType bookingType;

    /** Set when bookingType is CONSULTATION. null otherwise */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** Set when bookingType is PACKAGE. Null otherwise. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private HealthPackage healthPackage;

    /** Set when bookingType is PACKAGE. Optional even then. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_tier_id")
    private PackageTier packageTier;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    /** Patient-supplied notes (symptoms, special requests, etc.) */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Internal admin notes (confirmed time, follow-ups, etc.) — never returned to patient */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
