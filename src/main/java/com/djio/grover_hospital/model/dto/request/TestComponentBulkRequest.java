package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Bulk replace all components for a result. Convenient for entering a full
 * panel (e.g. a CBC) in one request. Replaces the entire set — any existing
 * components for the result are removed and re-created from this list.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestComponentBulkRequest {

    @NotNull(message = "Components list is required (may be empty to clear all)")
    @Valid
    private List<TestComponentRequest> components;
}