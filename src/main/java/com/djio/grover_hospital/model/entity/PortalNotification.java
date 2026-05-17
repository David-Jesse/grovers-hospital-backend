package com.djio.grover_hospital.model.entity;


import com.djio.grover_hospital.model.enums.PortalNotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * In-portal notification displayed in the bell dropdown and dashboard panel.
 * Polymorphic - same entity serves both patients and admins, distinguished by user_type.
 * Auto-cleanup removes older than 90 days
 */
@Entity
@Table(name = "portal_notifications", indexes = {
        @Index(name = "idx_portal_notif_user", columnList = "user_id, user_type"),
        @Index(name = "idx_portal_notif_read", columnList = "is_read"),
        @Index(name = "idx_portal_notif_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PortalNotificationType type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
