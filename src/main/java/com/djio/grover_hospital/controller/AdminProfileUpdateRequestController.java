package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.ReviewProfileUpdateRequestDto;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ProfileUpdateRequestResponse;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import com.djio.grover_hospital.service.ProfileUpdateRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/profile-update-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Profile Update Requests",
        description = "Review patient requests to change admin-only health-profile fields.")
public class AdminProfileUpdateRequestController {

    private final ProfileUpdateRequestService service;

    @GetMapping
    @Operation(summary = "List update requests (paginated, optional status filter)",
            description = "Filter by status=PENDING/APPROVED/REJECTED. Default page size 20, max 100.")
    public ResponseEntity<ApiResponse<Page<ProfileUpdateRequestResponse>>> list(
            @RequestParam(required = false) ProfileUpdateStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProfileUpdateRequestResponse> result = service.listForAdmin(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one update request in detail")
    public ResponseEntity<ApiResponse<ProfileUpdateRequestResponse>> getById(@PathVariable Long id) {
        ProfileUpdateRequestResponse request = service.getByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Request retrieved", request));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a request",
            description = "Applies the proposed value to the patient's health profile (for blood group / genotype / allergies). " +
                    "OTHER requests are marked handled but applied manually by the admin.")
    public ResponseEntity<ApiResponse<ProfileUpdateRequestResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewProfileUpdateRequestDto dto,
            HttpServletRequest httpRequest) {
        ProfileUpdateRequestResponse result = service.approve(id, dto, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Request approved and applied", result));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a request")
    public ResponseEntity<ApiResponse<ProfileUpdateRequestResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewProfileUpdateRequestDto dto,
            HttpServletRequest httpRequest) {
        ProfileUpdateRequestResponse result = service.reject(id, dto, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Request rejected", result));
    }
}