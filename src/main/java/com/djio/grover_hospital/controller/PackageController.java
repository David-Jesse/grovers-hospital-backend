package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.HealthPackageResponse;
import com.djio.grover_hospital.service.HealthPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final HealthPackageService packageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthPackageResponse>>> getAllPackages() {
        return ResponseEntity.ok(ApiResponse.success(packageService.getAllActive()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<HealthPackageResponse>> getPackageBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(packageService.getBySlug(slug)));
    }
}