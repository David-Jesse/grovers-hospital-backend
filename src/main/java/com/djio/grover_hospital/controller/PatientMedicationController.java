package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.MedicationResponse;
import com.djio.grover_hospital.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/medications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Medications",
        description = "Read-only access to the patient's own medication list. Updates are admin-only.")
public class PatientMedicationController {

    private final MedicationService medicationService;

    @GetMapping
    @Operation(summary = "Get my medications",
            description = "Returns all medications for the current patient. Pass ?activeOnly=true to filter to currently active ones.")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getMyMedications(
            @RequestParam(required = false) Boolean activeOnly) {
        List<MedicationResponse> meds = medicationService.getMyMedications(activeOnly);
        return ResponseEntity.ok(ApiResponse.success("Medications retrieved", meds));
    }
}