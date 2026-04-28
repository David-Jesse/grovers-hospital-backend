package com.djio.grover_hospital.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Access log entity for audit trail
@Entity
@Table(name = "access_logs", indexes = {
        @Index(name = "idx_access_logs_user", columnList = "user_id, user_type"),
        @Index(name = "idx_access_logs_resource", columnList = "resource_type, resource_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
