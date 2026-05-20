package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.MedicationRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.MedicationResponse;
import com.djio.grover_hospital.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Medications", description = "Admin CRUD for patient medications.")
public class AdminMedicationController {

    private final MedicationService medicationService;

    @GetMapping("/patients/{patientId}/medications")
    @Operation(summary = "List medications for a patient")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> listForPatient(@PathVariable Long patientId) {
        List<MedicationResponse> meds = medicationService.getMedicationsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Medications retrieved", meds));
    }

    @PostMapping("/patients/{patientId}/medications")
    @Operation(summary = "Add a medication to a patient")
    public ResponseEntity<ApiResponse<MedicationResponse>> create(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicationRequest request,
            HttpServletRequest httpRequest) {
        MedicationResponse created = medicationService.createForPatient(patientId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Medication created", created));
    }

    @PutMapping("/medications/{medicationId}")
    @Operation(summary = "Update a medication")
    public ResponseEntity<ApiResponse<MedicationResponse>> update(
            @PathVariable Long medicationId,
            @Valid @RequestBody MedicationRequest request,
            HttpServletRequest httpRequest) {
        MedicationResponse updated = medicationService.update(medicationId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Medication updated", updated));
    }

    @DeleteMapping("/medications/{medicationId}")
    @Operation(summary = "Delete a medication")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long medicationId,
            HttpServletRequest httpRequest) {
        medicationService.delete(medicationId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Medication deleted", null));
    }
}