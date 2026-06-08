package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.AdminFeedbackStatusUpdateRequest;
import com.djio.grover_hospital.model.dto.response.*;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Boolean unreadOnly) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getAllForAdmin(pageable, unreadOnly)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FeedbackStats>> stats() {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getStats()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getById(@PathVariable Long id) {
        feedbackService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getByIdForAdmin(id)));
    }

    @GetMapping("/filtered")
    @Operation(summary = "List feedback with rich filters",
            description = "Optional filters: source, status, type, isRead. Use this for the admin dashboard"
    )
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> listFiltered(
            @RequestParam(required = false) FeedbackSource source,
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) FeedbackType type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<FeedbackResponse> result = feedbackService.listForAdminFiltered(source, status, type, isRead, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", result));
    }

    // Admin Detail view with internal notes endpoint
    @GetMapping("/{id}/detail")
    @Operation(summary = "Get full admin detail for a feedback entry",
        description = "Includes adminInternalNotes, reviewer info, and full Patient contact info."
    )
    public ResponseEntity<ApiResponse<AdminFeedbackResponse>> getDetailForAdmin(@PathVariable Long id) {
        AdminFeedbackResponse detail = feedbackService.getDetailForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Feedback detail retrieved", detail));
    }

    // Portal-only status workflow update
    @PutMapping("/{id}/status")
    @Operation(summary = "Update feedback status (portal feedback only)",
        description = "Only valid for PORTAL feedback. Records the current admin as reviewer and timestamps the action"
    )
    public ResponseEntity<ApiResponse<AdminFeedbackResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminFeedbackStatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        AdminFeedbackResponse updated = feedbackService.updateStatus(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Feedback updated", updated));
    }


    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<FeedbackResponse>> toggleRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Read state toggled", feedbackService.toggleRead(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Feedback deleted", null));
    }
}