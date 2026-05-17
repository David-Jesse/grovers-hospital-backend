package com.djio.grover_hospital.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalNotificationSummary {
    private long unreadCount;
    private long totalCount;
}