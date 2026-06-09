package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.response.AdminProfileResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/me")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminRepository adminRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminProfileResponse>> getMyProfile() {
        Long adminId = SecurityUtils.getCurrentUserId();
        AdminProfileResponse profile = adminRepository.findById(adminId)
                .map(AdminProfileResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}