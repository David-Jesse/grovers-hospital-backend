package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.PortalNotification;
import com.djio.grover_hospital.model.enums.PortalNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalNotificationResponse {

    private Long id;
    private PortalNotificationType type;
    private String message;
    private Boolean isRead;
    private OffsetDateTime createdAt;

    public static PortalNotificationResponse from(PortalNotification notification) {
        return PortalNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}