package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.model.enums.PreferredContactMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PortalFeedbackRequest {

    @Size(max = 300)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;

    @NotNull(message = "Feedback type is required")
    private FeedbackType type;

    /**
     * Optional 1-5 star rating
     */
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Short rating;

    private Boolean responseWanted;

    private PreferredContactMethod preferredContactMethod;
}