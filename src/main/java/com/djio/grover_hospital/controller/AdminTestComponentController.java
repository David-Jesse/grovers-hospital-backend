package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.request.TestComponentBulkRequest;
import com.djio.grover_hospital.model.dto.request.TestComponentRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TestComponentResponse;
import com.djio.grover_hospital.service.TestComponentService;
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
@Tag(name = "Admin - Test Components",
        description = "Admin CRUD for structured lab result components, plus bulk replace"
)
public class AdminTestComponentController {

    private final TestComponentService testComponentService;

    @GetMapping("/results/{resultId}/components")
    @Operation(summary = "List components for a result")
    public ResponseEntity<ApiResponse<List<TestComponentResponse>>> list(@PathVariable Long resultId) {
        List<TestComponentResponse> components = testComponentService.getComponentsForResult(resultId);
        return ResponseEntity.ok(ApiResponse.success("Components retrieved", components));
    }

    @PostMapping("/results/{resultId}/components")
    @Operation(summary = "Add a single component to a result")
    public ResponseEntity<ApiResponse<TestComponentResponse>> add(
            @PathVariable Long resultId,
            @Valid @RequestBody TestComponentRequest request,
            HttpServletRequest httpRequest
    ) {
        TestComponentResponse created = testComponentService.addToResult(resultId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Component added", created));
    }

    @PutMapping("/results/{resultId}/components/bulk")
    @Operation(summary = "Bulk replace all components for a result",
            description = "Clears existing components and re-creates from the supplied list. " +
                    "Send an empty list to clear all. displayOrder defaults to list position if omitted.")
    public ResponseEntity<ApiResponse<List<TestComponentResponse>>> bulkReplace(
            @PathVariable Long resultId,
            @Valid @RequestBody TestComponentBulkRequest request,
            HttpServletRequest httpRequest
    ) {
        List<TestComponentResponse> result = testComponentService.replaceAllForResult(resultId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Components replace", result));
    }

    @PutMapping("/components/{componentId}")
    @Operation(summary = "Update a single component")
    public ResponseEntity<ApiResponse<TestComponentResponse>> update(
            @PathVariable Long componentId,
            @Valid @RequestBody TestComponentRequest request,
            HttpServletRequest httpRequest
    ) {
        TestComponentResponse updated = testComponentService.update(componentId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Component updated", updated));
    }

    @DeleteMapping("/components/{componentId}")
    @Operation(summary = "Delete a single component")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long componentId,
            HttpServletRequest httpRequest
    ) {
        testComponentService.delete(componentId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Component deleted", null));
    }
}