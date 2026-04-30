package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.HealthPackageRequest;
import com.djio.grover_hospital.model.dto.request.PackageTierRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.HealthPackageResponse;
import com.djio.grover_hospital.model.dto.response.PackageTierResponse;
import com.djio.grover_hospital.service.HealthPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AdminPackageController {

    private final HealthPackageService packageService;

    // === Packages ===

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthPackageResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(packageService.getAllForAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HealthPackageResponse>> create(
            @Valid @RequestBody HealthPackageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Package created", packageService.createPackage(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthPackageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody HealthPackageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Package updated", packageService.updatePackage(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.ok(ApiResponse.success("Package deleted", null));
    }

    // === Tiers ===

    @PostMapping("/{packageId}/tiers")
    public ResponseEntity<ApiResponse<PackageTierResponse>> addTier(
            @PathVariable Long packageId,
            @Valid @RequestBody PackageTierRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tier added", packageService.addTier(packageId, request)));
    }

    @PutMapping("/tiers/{tierId}")
    public ResponseEntity<ApiResponse<PackageTierResponse>> updateTier(
            @PathVariable Long tierId,
            @Valid @RequestBody PackageTierRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Tier updated", packageService.updateTier(tierId, request)));
    }

    @DeleteMapping("/tiers/{tierId}")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable Long tierId) {
        packageService.deleteTier(tierId);
        return ResponseEntity.ok(ApiResponse.success("Tier deleted", null));
    }
}