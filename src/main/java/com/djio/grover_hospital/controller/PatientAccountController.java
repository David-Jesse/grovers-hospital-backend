package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.AccountDeletionRequestDto;
import com.djio.grover_hospital.model.dto.request.CancelDeletionRequest;
import com.djio.grover_hospital.model.dto.response.AccountDeletionStatusResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DataExportJobResponse;
import com.djio.grover_hospital.service.AccountDeletionService;
import com.djio.grover_hospital.service.DataExportService;
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
@RequestMapping("/portal/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Account",
        description = "Account self-service: deletion request, cancel deletion, data export.")
public class PatientAccountController {

    private final AccountDeletionService accountDeletionService;
    private final DataExportService dataExportService;

    // ======== Account deletion ==========

    @PostMapping("/delete-request")
    @Operation(summary = "Request account deletion",
            description = "Requires password confirmation. Account stays usable for 30 days, " +
                    "then is hard-deleted by a daily cron. Cancel within the window via /cancel-deletion.")
    public ResponseEntity<ApiResponse<AccountDeletionStatusResponse>> requestDeletion(
            @Valid @RequestBody AccountDeletionRequestDto request,
            HttpServletRequest httpRequest
    ) {
        AccountDeletionStatusResponse response = accountDeletionService.requestDeletion(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Account deletion scheduled. You have 30 days to cancel this request.", response
        ));
    }

    @PostMapping("/cancel-deletion")
    @Operation(summary = "Cancel pending account deletion",
            description = "Requires password. Removes the pending deletion request entirely.")
    public ResponseEntity<ApiResponse<Void>> cancelDeletion(
            @Valid @RequestBody CancelDeletionRequest request,
            HttpServletRequest httpRequest) {
        accountDeletionService.cancelDeletion(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Account deletion cancelled.", null));
    }

    @GetMapping("/delete-status")
    @Operation(summary = "Check whether a pending deletion exists for the current account")
    public ResponseEntity<ApiResponse<AccountDeletionStatusResponse>> getDeletionStatus() {
        AccountDeletionStatusResponse status = accountDeletionService.getMyPendingDeletion();
        return ResponseEntity.ok(ApiResponse.success(status != null ? "Pending deletion found" :
                "No pending deletion", status));
    }

    // =========== Data Export ==========

    @PostMapping("/export-data")
    @Operation(summary = "Request a personal data export",
            description = "Generates a JSON dump of all your data and emails a download link. " +
                    "Link expires in 7 days. Encrypted lab result file contents are not included; " +
                    "only metadata so you know which results exist.")
    public ResponseEntity<ApiResponse<DataExportJobResponse>> requestReport(HttpServletRequest httpRequest) {
        DataExportJobResponse job = dataExportService.requestExport(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Export started. Check your email for the download link in a few minutes.", job
        ));
    }
}
