package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.VisitRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.VisitResponse;
import com.djio.grover_hospital.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Visits",
        description = "Admin endpoints to view and fill in visit details. " +
                "Visits are auto-stubbed when a booking is marked COMPLETED.")
public class AdminVisitController {

    private final VisitService visitService;

    @GetMapping("/patients/{patientId}/visits")
    @Operation(summary = "List all visits for a patient")
    public ResponseEntity<ApiResponse<List<VisitResponse>>> listForPatient(@PathVariable Long patientId) {
        List<VisitResponse> visits = visitService.getVisitsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Visits retrieved", visits));
    }

    @GetMapping("/visits/{visitId}")
    @Operation(summary = "Get a visit by id")
    public ResponseEntity<ApiResponse<VisitResponse>> getById(@PathVariable Long visitId) {
        VisitResponse visit = visitService.getById(visitId);
        return ResponseEntity.ok(ApiResponse.success("Visit retrieved", visit));
    }

    @GetMapping("/bookings/{bookingId}/visit")
    @Operation(summary = "Get the visit linked to a specific booking",
            description = "Useful right after marking a booking COMPLETED - returns the autp-stubbed visit ready for filling in"
    )
    public ResponseEntity<ApiResponse<VisitResponse>> getMyBooking(@PathVariable Long bookingId) {
        VisitResponse visit = visitService.getByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Visit retrieved", visit));
    }

    @PutMapping("/visits/{visitId}")
    @Operation(summary = "Update a visit",
            description = "Fill in or amend diagnosis, treatment, clinical notes, attending doctor, and follow-up info.")
    public ResponseEntity<ApiResponse<VisitResponse>> update(
            @PathVariable Long visitId,
            @Valid @RequestBody VisitRequest request,
            HttpServletRequest httpRequest) {
        VisitResponse updated = visitService.update(visitId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Visit updated", updated));
    }
}
