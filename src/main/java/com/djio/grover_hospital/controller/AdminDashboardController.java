package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DashboardStatsResponse;
import com.djio.grover_hospital.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats()));
    }
}