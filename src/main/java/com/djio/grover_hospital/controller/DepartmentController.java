package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DepartmentResponse;
import com.djio.grover_hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllActive()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getBySlug(slug)));
    }
}
