package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.NotificationPreferencesRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.NotificationPreferencesResponse;
import com.djio.grover_hospital.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/notification-preferences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Notification Preferences",
        description = "Per-event channel toggles for notifications. " +
                "Patient opt-out layer on top of the app-level kill switches.")
public class PatientNotificationPreferencesController {

    private final NotificationPreferenceService service;

    @GetMapping
    @Operation(summary = "Get my notification preferences",
            description = "Auto-creates from app defaults on first call; never returns 404.")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getMyPreferences() {
        NotificationPreferencesResponse prefs = service.getMyPreferences();
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved", prefs));
    }

    @PutMapping
    @Operation(summary = "Update my notification preferences",
            description = "Partial update — only fields present in the body are changed. " +
                    "Send null or omit a field to leave it untouched.")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updateMyPreferences(
            @Valid @RequestBody NotificationPreferencesRequest request,
            HttpServletRequest httpRequest
    ) {
        NotificationPreferencesResponse updated = service.updateMyPreferences(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", updated));
    }
}
