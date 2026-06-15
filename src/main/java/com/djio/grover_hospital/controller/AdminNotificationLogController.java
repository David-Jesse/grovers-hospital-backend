package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.NotificationDeliveryLogResponse;
import com.djio.grover_hospital.model.enums.DeliveryChannel;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.service.AuditService;
import com.djio.grover_hospital.service.NotificationDeliveryLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoint for inspecting notification delivery history.
 * Useful for support investigations ("did the patient receive the reminder?")
 * and operational dashboards.
 *
 * <p>Auth: relies on URL-pattern protection in your SecurityConfig
 * (the existing /admin/** rules already require ADMIN role).</p>
 */
@RestController
@RequestMapping("/admin/notification-logs")
@RequiredArgsConstructor
public class AdminNotificationLogController {

    private final NotificationDeliveryLogService logService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDeliveryLogResponse>>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) DeliveryChannel channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        Page<NotificationDeliveryLogResponse> result = logService.list(patientId, channel, page, size);

        auditService.log(
                SecurityUtils.getCurrentUserId(),
                "ADMIN",
                "LIST_NOTIFICATION_LOGS",
                "NOTIFICATION_DELIVERY_LOG",
                null,
                request
        );

        return ResponseEntity.ok(ApiResponse.success("Notification logs retrieved", result));
    }
}