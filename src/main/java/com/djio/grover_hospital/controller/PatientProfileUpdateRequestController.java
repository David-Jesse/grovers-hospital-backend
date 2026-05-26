package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.CreateProfileUpdateRequestDto;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.ProfileUpdateRequestResponse;
import com.djio.grover_hospital.service.ProfileUpdateRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/profile-update-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Profile Update Requests",
        description = "Request changes to admin-only health-profile fields (blood group, genotype, allergies). Admin approves or rejects.")
public class PatientProfileUpdateRequestController {

    private final ProfileUpdateRequestService service;

    @PostMapping
    @Operation(summary = "Submit a profile update request",
            description = "Propose a change to an admin-only field. For OTHER, include otherFieldDescription. " +
                    "Status starts PENDING until an admin reviews it.")
    public ResponseEntity<ApiResponse<ProfileUpdateRequestResponse>> submit(
            @Valid @RequestBody CreateProfileUpdateRequestDto dto,
            HttpServletRequest httpRequest) {
        ProfileUpdateRequestResponse created = service.submit(dto, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Update request submitted", created));
    }

    @GetMapping
    @Operation(summary = "List my update requests")
    public ResponseEntity<ApiResponse<List<ProfileUpdateRequestResponse>>> getMyRequests() {
        List<ProfileUpdateRequestResponse> requests = service.getMyRequests();
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved", requests));
    }
}