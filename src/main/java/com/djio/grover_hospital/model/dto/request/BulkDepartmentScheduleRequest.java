package com.djio.grover_hospital.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkDepartmentScheduleRequest {

    @NotNull(message = "Schedules list is required (use empty array to clear)")
    @Valid
    private List<DepartmentScheduleRequest> schedules;
}