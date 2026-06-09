package com.djio.grover_hospital.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long unreadFeedback;
    private long pendingAppointments;
    private long profileUpdatesPending;
    private long articleDrafts;
}