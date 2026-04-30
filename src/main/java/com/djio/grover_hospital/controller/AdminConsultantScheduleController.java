package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.ConsultantScheduleRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ConsultantScheduleResponse;
import com.djio.grover_hospital.service.ConsultantScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/consultant-schedule")
@RequiredArgsConstructor
public class AdminConsultantScheduleController {

    private final ConsultantScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultantScheduleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAllForAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConsultantScheduleResponse>> create(@Valid @RequestBody ConsultantScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Schedule created", scheduleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsultantScheduleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ConsultantScheduleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Schedule updated", scheduleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule deleted", null));
    }
}
