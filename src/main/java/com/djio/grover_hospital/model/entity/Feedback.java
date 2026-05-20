package com.djio.grover_hospital.model.entity;


import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.model.enums.PreferredContactMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "feedback", indexes = {
        @Index(name = "idx_feedback_is_read", columnList = "is_read"),
        @Index(name = "idx_feedback_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optional - set when feedback comes from a logged-in patient
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /**
     * Required for HOMEPAGE source, optional for PORTAL
     */
    @Column(length = 150)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 300)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FeedbackSource source = FeedbackSource.HOMEPAGE;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // ============================================================
    // PORTAL-only expansion fields. All nullable; HOMEPAGE submissions
    // leave them blank.
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private FeedbackType type;

    /** Optional 1-5 star rating. */
    @Column(name = "rating")
    private Short rating;

    @Column(name = "response_wanted")
    private Boolean responseWanted;

    /** Only meaningful if responseWanted = true. */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", length = 20)
    private PreferredContactMethod preferredContactMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FeedbackStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private Admin reviewedByAdmin;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    /** Admin-only field. Never exposed in patient-facing responses. */
    @Column(name = "admin_internal_notes", columnDefinition = "TEXT")
    private String adminInternalNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}