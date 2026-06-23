package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.AdminNotesRequest;
import com.djio.grover_hospital.model.dto.request.BookingStatusUpdateRequest;
import com.djio.grover_hospital.model.dto.request.ConfirmBookingRequest;
import com.djio.grover_hospital.model.dto.request.UpdateAppointmentTimeRequest;
import com.djio.grover_hospital.model.dto.response.AdminBookingResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.BookingResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminBookingResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) BookingStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAllForAdmin(pageable, status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getByIdForAdmin(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", bookingService.updateStatus(id, request)));
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> updateNotes(
            @PathVariable Long id,
            @Valid @RequestBody AdminNotesRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notes updated", bookingService.updateAdminNotes(id, request.getAdminNotes())));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirm(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmBookingRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking confirmed",
                bookingService.confirmWithTime(id, request)
        ));
    }

    @PutMapping("/{id}/appointment-time")
    public ResponseEntity<ApiResponse<BookingResponse>> updateAppointmentTime(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentTimeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment time updated",
                bookingService.updateAppointmentTime(id, request)
        ));
    }
}