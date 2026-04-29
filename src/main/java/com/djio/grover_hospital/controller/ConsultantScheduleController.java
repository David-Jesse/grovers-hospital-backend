package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ConsultantScheduleResponse;
import com.djio.grover_hospital.service.ConsultantScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/consultant-schedule")
@RequiredArgsConstructor
public class ConsultantScheduleController {

    private final ConsultantScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultantScheduleResponse>>> getAllSchedules() {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAllActive()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<ConsultantScheduleResponse>>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getByDepartment(departmentId)));
    }
}