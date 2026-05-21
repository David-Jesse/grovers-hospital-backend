package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.AccountDeletionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "account_deletion_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountDeletionStatus status = AccountDeletionStatus.PENDING_DELETION;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "scheduled_for", nullable = false)
    private OffsetDateTime scheduledFor;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.status == null) this.status = AccountDeletionStatus.PENDING_DELETION;
    }
}
