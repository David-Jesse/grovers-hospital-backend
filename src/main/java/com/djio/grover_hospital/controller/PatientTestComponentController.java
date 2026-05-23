package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TestComponentResponse;
import com.djio.grover_hospital.service.TestComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/results/{resultId}/components")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Result Components",
        description = "Read-only structured test values for one of the patient's own results.")
public class PatientTestComponentController {

    private final TestComponentService testComponentService;

    @GetMapping
    @Operation(summary = "Get structured components for one of my results",
            description = "Returns the parsed test values (name, value, unit, reference range, flag) " +
                    "for a result the patient owns. Ownership is enforced.")
    public ResponseEntity<ApiResponse<List<TestComponentResponse>>> getMyResultComponents(
            @PathVariable Long resultId
    ) {
        List<TestComponentResponse> components = testComponentService.getMyResultComponents(resultId);
        return ResponseEntity.ok(ApiResponse.success("Components retrieved", components));
    }
}
