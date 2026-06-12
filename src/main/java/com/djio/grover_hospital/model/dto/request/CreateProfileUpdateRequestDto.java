package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.ProfileUpdateField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateProfileUpdateRequestDto {

    @NotNull(message = "Target field is required")
    private ProfileUpdateField targetField;

    /**
     * Required only when targetField = OTHER. Describes which field the patient means.
     */
    @Size(max = 200)
    private String otherFieldDescription;

    @NotBlank(message = "Proposed value is required")
    @Size(max = 500)
    private String proposedValue;

    @Size(max = 2000)
    private String patientNote;
}