package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.ChronicConditionRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ChronicConditionResponse;
import com.djio.grover_hospital.service.ChronicConditionService;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Chronic Conditions",
        description = "Admin CRUD for patient chronic conditions.")
public class AdminChronicConditionController {

    private final ChronicConditionService chronicConditionService;

    @GetMapping("/patients/{patientId}/conditions")
    @Operation(summary = "List chronic conditions for a patient")
    public ResponseEntity<ApiResponse<List<ChronicConditionResponse>>> listForPatient(@PathVariable Long patientId) {
        List<ChronicConditionResponse> conditions = chronicConditionService.getConditionsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Chronic conditions retrieved", conditions));
    }

    @PostMapping("/patients/{patientId}/conditions")
    @Operation(summary = "Add a chronic condition to a patient")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> create(
            @PathVariable Long patientId,
            @Valid @RequestBody ChronicConditionRequest request,
            HttpServletRequest httpRequest
    ) {
        ChronicConditionResponse created = chronicConditionService.createForPatient(patientId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition created", created));
    }

    @PutMapping("/conditions/{conditionId}")
    @Operation(summary = "Update a chronic condition")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> update(
            @PathVariable Long conditionId,
            @Valid @RequestBody ChronicConditionRequest request,
            HttpServletRequest httpRequest
    ) {
        ChronicConditionResponse updated = chronicConditionService.update(conditionId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition updated", updated));
    }

    @DeleteMapping("/conditions/{conditionId}")
    @Operation(summary = "Delete a chronic condition")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long conditionId,
            HttpServletRequest httpRequest
    ) {
        chronicConditionService.delete(conditionId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition deleted", null));
    }
}
