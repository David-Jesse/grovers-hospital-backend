package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.RescheduleBookingRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.BookingResponse;
import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.service.BookingRescheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Booking Reschedule",
        description = "Patient self-service reschedule. Resets the booking to PENDING for admin re-confirmation.")
public class PatientBookingRescheduleController {

    private final BookingRescheduleService bookingRescheduleService;

    @PutMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule one of my bookings",
            description = "Changes the preferred date and resets status to PENDING. " +
                    "Only PENDING or CONFIRMED bookings can be rescheduled.")
    public ResponseEntity<ApiResponse<BookingResponse>> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleBookingRequest request,
            HttpServletRequest httpRequest) {
        Booking updated = bookingRescheduleService.reschedule(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Booking rescheduled. It is now pending re-confirmation by our team.",
                BookingResponse.from(updated)));
    }
}