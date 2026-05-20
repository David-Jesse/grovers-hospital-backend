package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ChronicConditionResponse;
import com.djio.grover_hospital.service.ChronicConditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/conditions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Chronic Conditions",
        description = "Read-only access to the patient's own chronic condition list.")
public class PatientChronicConditionController {

    private final ChronicConditionService chronicConditionService;

    @GetMapping
    @Operation(summary = "Get my chronic conditions")
    public ResponseEntity<ApiResponse<List<ChronicConditionResponse>>> getMyConditions() {
        List<ChronicConditionResponse> conditions = chronicConditionService.getMyConditions();
        return ResponseEntity.ok(ApiResponse.success("Chronic conditions retrieved", conditions));
    }
}