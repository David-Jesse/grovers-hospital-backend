package com.djio.grover_hospital.model.entity;


import com.djio.grover_hospital.model.enums.FeedbackSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
