package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.ConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChronicConditionRequest {

    @NotBlank(message = "Condition name is required")
    @Size(max = 200)
    private String name;

    private LocalDate diagnosedDate;

    private ConditionStatus status;

    private String notes;

    /** Optional internal doctor FK. If null, managingDoctorText is used instead. */
    private Long managingDoctorId;

    @Size(max = 200)
    private String managingDoctorText;
}