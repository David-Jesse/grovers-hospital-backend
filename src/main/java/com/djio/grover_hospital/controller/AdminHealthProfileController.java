package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.AdminHealthProfileRequest;
import com.djio.grover_hospital.model.dto.response.AdminHealthProfileResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.service.HealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/patients/{id}/health-profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Patient Health Profile",
        description = "Admin endpoints to view and edit any patient's full health profile, including clinical fields.")
public class AdminHealthProfileController {

    private final HealthProfileService healthProfileService;

    @GetMapping
    @Operation(summary = "Get a patient's health profile",
            description = "Returns the full health profile including clinical notes. Auto-creates an empty profile if no exists."
    )
    public ResponseEntity<ApiResponse<AdminHealthProfileResponse>> getProfileForPatient(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        AdminHealthProfileResponse profile = healthProfileService.getProfileForPatient(id, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Health profile retrieved", profile));
    }

    @PutMapping
    @Operation(summary = "Update a patient's health profile",
            description = "Updates all fields including clinical ones (blood group, genotype, allergies, clinical notes).")
    public ResponseEntity<ApiResponse<AdminHealthProfileResponse>> updateProfileForPatient(
            @PathVariable Long id,
            @Valid @RequestBody AdminHealthProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        AdminHealthProfileResponse updated = healthProfileService.updateProfileForPatient(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Health profile updated", updated));
    }
}
