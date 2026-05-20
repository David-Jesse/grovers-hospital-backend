package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private FeedbackStatus status;

    /**
     * Optional internal admin notes. Never shown to patient
     */
    private String adminInternalNotes;
}