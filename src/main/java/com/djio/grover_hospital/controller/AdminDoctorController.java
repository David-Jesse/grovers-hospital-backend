package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.DoctorRequest;
import com.djio.grover_hospital.model.dto.response.AdminDoctorResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminDoctorResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllForAdmin()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminDoctorResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getByIdForAdmin(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminDoctorResponse>> create(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor created", doctorService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminDoctorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Doctor updated", doctorService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor deleted", null));
    }
}
