package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.NotificationDeliveryLog;
import com.djio.grover_hospital.model.enums.DeliveryChannel;
import com.djio.grover_hospital.model.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationDeliveryLogResponse {

    private Long id;
    private Long patientId;
    private String eventName;
    private String referenceType;
    private Long referenceId;
    private DeliveryChannel channel;
    private DeliveryStatus status;
    private String providerMessageId;
    private String recipient;
    private String errorMessage;
    private OffsetDateTime sentAt;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static NotificationDeliveryLogResponse from(NotificationDeliveryLog log) {
        return NotificationDeliveryLogResponse.builder()
                .id(log.getId())
                .patientId(log.getPatientId())
                .eventName(log.getEventName())
                .referenceType(log.getReferenceType())
                .referenceId(log.getReferenceId())
                .channel(log.getChannel())
                .status(log.getStatus())
                .providerMessageId(log.getProviderMessageId())
                .recipient(log.getRecipient())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .deliveredAt(log.getDeliveredAt())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}