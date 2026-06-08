package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.AdminUpdatePatientRequest;
import com.djio.grover_hospital.model.dto.response.AdminPatientResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.AdminPatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/patients")
@RequiredArgsConstructor
public class AdminPatientController {

    private final AdminPatientService adminPatientService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminPatientResponse>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminPatientService.search(search, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPatientResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminPatientService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPatientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdatePatientRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Patient updated", adminPatientService.update(id, request, httpRequest)));
    }
}