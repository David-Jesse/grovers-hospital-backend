package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.TestComponentFlag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestComponentRequest {

    @NotBlank(message = "Component name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 100)
    private String value;

    @Size(max = 50)
    private String unit;

    @Size(max = 150)
    private String referenceRange;

    private TestComponentFlag flag;

    private String notes;

    /** Optional ordering hint for display. Defaults to 0. */
    private Integer displayOrder;
}