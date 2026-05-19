package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.PatientHealthProfileRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.HealthProfileResponse;
import com.djio.grover_hospital.service.HealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/health-profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Health Profile",
        description = "Patient self-service endpoints for viewing and updating their health profile. " +
                "Clinical fields (blood group, genotype, allergies, clinical notes) are read-only here and managed by admin.")
public class PatientHealthProfileController {

    private final HealthProfileService healthProfileService;

    @GetMapping
    @Operation(summary = "Get my health profile",
            description = "Returns the current patient's health profile. Auto-creates an empty profile on first call, so this never returns 404.")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> getMyProfile() {
        HealthProfileResponse profile = healthProfileService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success( "Health profile retrieved", profile));
    }

    @PutMapping
    @Operation(summary = "Update my health profile",
            description = "Updates self-reported fields only (height, weight, emergency contact). " +
                    "Clinical fields are ignored even if sent.")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> updateMyProfile(
            @Valid @RequestBody PatientHealthProfileRequest request,
            HttpServletRequest httpRequest) {
        HealthProfileResponse updated = healthProfileService.updateMyProfile(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Health profile updated", updated));
    }
}