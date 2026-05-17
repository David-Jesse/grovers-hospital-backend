package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.dto.response.PortalNotificationResponse;
import com.djio.grover_hospital.model.dto.response.PortalNotificationSummary;
import com.djio.grover_hospital.service.PortalNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/notifications")
@RequiredArgsConstructor
public class PatientNotificationController {

    private final PortalNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PortalNotificationResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Boolean unreadOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(pageable, unreadOnly)
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PortalNotificationSummary>> summary() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMySummary()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<PortalNotificationResponse>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Marked as read", notificationService.markAsRead(id)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        int updated = notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

}
