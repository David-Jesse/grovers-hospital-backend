package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.ChangePasswordRequest;
import com.djio.grover_hospital.model.dto.request.PatientProfileResponse;
import com.djio.grover_hospital.model.dto.request.UpdateProfileRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/profile")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(profileService.getCurrentProfile()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Profile Updated", profileService.updateCurrentProfile(request)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updatePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}