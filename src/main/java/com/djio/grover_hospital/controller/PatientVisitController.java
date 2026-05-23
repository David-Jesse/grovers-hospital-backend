package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.PatientVisitResponse;
import com.djio.grover_hospital.service.PatientVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/visits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Past Visits",
        description = "Read-only access to the patient's own documented past visits.")
public class PatientVisitController {

    private final PatientVisitService patientVisitService;

    @GetMapping
    @Operation(summary = "List my past visits",
        description = "Returns documented visits (empty auto-stubs are hidden), newest first."
    )
    public ResponseEntity<ApiResponse<List<PatientVisitResponse>>> getMyVisits() {
        List<PatientVisitResponse> visits = patientVisitService.getMyVisits();
        return ResponseEntity.ok(ApiResponse.success("Visits retrieved", visits));
    }

    @GetMapping("/{visitId}")
    @Operation(summary = "Get one of my visits in detail")
    public ResponseEntity<ApiResponse<PatientVisitResponse>> getMyVisit(@PathVariable Long visitId) {
        PatientVisitResponse visit = patientVisitService.getMyVisit(visitId);
        return ResponseEntity.ok(ApiResponse.success("Visits retrieved", visit));
    }
}
