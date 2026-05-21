package com.djio.grover_hospital.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // Booking confirmation
    @Column(name = "booking_confirmation_email", nullable = false)
    @Builder.Default
    private Boolean bookingConfirmationEmail = true;

    @Column(name = "booking_confirmation_sms", nullable = false)
    @Builder.Default
    private Boolean bookingConfirmationSms = false;

    @Column(name = "booking_confirmation_whatsapp", nullable = false)
    @Builder.Default
    private Boolean bookingConfirmationWhatsapp = true;

    // Booking status update
    @Column(name = "booking_status_update_email", nullable = false)
    @Builder.Default
    private Boolean bookingStatusUpdateEmail = true;

    @Column(name = "booking_status_update_sms", nullable = false)
    @Builder.Default
    private Boolean bookingStatusUpdateSms = true;

    @Column(name = "booking_status_update_whatsapp", nullable = false)
    @Builder.Default
    private Boolean bookingStatusUpdateWhatsapp = true;

    // Result ready
    @Column(name = "result_ready_email", nullable = false)
    @Builder.Default
    private Boolean resultReadyEmail = true;

    @Column(name = "result_ready_sms", nullable = false)
    @Builder.Default
    private Boolean resultReadySms = true;

    @Column(name = "result_ready_whatsapp", nullable = false)
    @Builder.Default
    private Boolean resultReadyWhatsapp = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}