package com.djio.grover_hospital.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleBookingRequest {

    @NotNull(message = "New preferred date is required")
    @Future(message = "New preferred date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newPreferredDate;

    @Size(max = 1000)
    private String reason;
}
