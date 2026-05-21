package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.PortalFeedbackRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.FeedbackResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/feedback")
@RequiredArgsConstructor
public class PatientFeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            @Valid @RequestBody PortalFeedbackRequest request,
            HttpServletRequest httpRequest
    ) {
        FeedbackResponse created = feedbackService.submitPortalFeedback(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getMyFeedback(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<FeedbackResponse> history = feedbackService.getMyFeedback(pageable);
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", history));
    }
}