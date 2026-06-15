package com.djio.grover_hospital.model.entity;

import com.djio.grover_hospital.model.enums.DeliveryChannel;
import com.djio.grover_hospital.model.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Per-attempt audit record of an outbound notification.
 *
 * <p>One row is written when a send is initiated (status QUEUED), updated to SENT once
 * the provider accepts it, and updated again to DELIVERED/FAILED/BOUNCED when a delivery
 * receipt (DLR) is received. Email rows typically remain at SENT since SMTP has no
 * built-in delivery callback.</p>
 */
@Entity
@Table(name = "notification_delivery_logs", indexes = {
        @Index(name = "idx_notification_logs_patient", columnList = "patient_id, created_at DESC"),
        @Index(name = "idx_notification_logs_status", columnList = "status, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** Canonical event name from {@link com.djio.grover_hospital.model.enums.NotificationEvent}. */
    @Column(name = "event_name", nullable = false, length = 64)
    private String eventName;

    /** Optional FK-ish reference to the domain object that triggered this send (e.g. BOOKING, RESULT). */
    @Column(name = "reference_type", length = 32)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryStatus status;

    /**
     * Provider-side identifier returned on submission. For SMPP this is the message_id from
     * the submit_sm response; used to correlate later DLRs.
     */
    @Column(name = "provider_message_id", length = 128)
    private String providerMessageId;

    /** Recipient address (email, phone in E.164, etc.) — handy for support investigations. */
    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}