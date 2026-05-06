package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackStats;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.FeedbackService;
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