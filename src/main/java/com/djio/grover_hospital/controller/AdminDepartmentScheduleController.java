package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.BulkDepartmentScheduleRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DepartmentScheduleResponse;
import com.djio.grover_hospital.service.DepartmentScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/departments/{departmentId}/schedule")
@RequiredArgsConstructor
public class AdminDepartmentScheduleController {

    private final DepartmentScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentScheduleResponse>>> get(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getByDepartment(departmentId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<List<DepartmentScheduleResponse>>> replace(
            @PathVariable Long departmentId,
            @Valid @RequestBody BulkDepartmentScheduleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Schedule updated",
                scheduleService.replaceForDepartment(departmentId, request)
        ));
    }
}