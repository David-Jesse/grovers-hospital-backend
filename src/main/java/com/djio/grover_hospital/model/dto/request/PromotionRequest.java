package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PromotionRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    private String title;

    @Size(max = 500)
    private String subtitle;

    private String description;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 500)
    private String linkUrl;

    private Integer displayOrder;

    private Boolean isActive;

    /** Optional. If null, the promotion is active immediately*/
    private OffsetDateTime startsAt;

    /** Optional. If null, the promotion runs indefinitely. */
    private OffsetDateTime endsAt;
}
