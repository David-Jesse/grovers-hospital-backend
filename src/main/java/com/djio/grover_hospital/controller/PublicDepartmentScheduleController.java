package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DepartmentScheduleResponse;
import com.djio.grover_hospital.service.DepartmentScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/departments")
@RequiredArgsConstructor
public class PublicDepartmentScheduleController {

    private final DepartmentScheduleService scheduleService;

    @GetMapping("/{departmentId}/schedule")
    public ResponseEntity<ApiResponse<List<DepartmentScheduleResponse>>> get(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getByDepartment(departmentId)));
    }
}