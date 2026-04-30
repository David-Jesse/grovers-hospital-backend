package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultantScheduleRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @Size(max = 200)
    private String consultantName;

    @NotBlank(message = "Schedule text is required")
    @Size(max = 300)
    private String scheduleText;

    private Integer displayOrder;

    private Boolean isActive;
}
