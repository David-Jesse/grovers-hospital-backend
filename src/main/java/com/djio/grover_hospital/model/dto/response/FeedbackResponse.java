package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private FeedbackSource source;
    private Boolean isRead;
    private Long patientId;
    private OffsetDateTime createdAt;

    public static FeedbackResponse from(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .name(feedback.getName())
                .email(feedback.getEmail())
                .subject(feedback.getSubject())
                .message(feedback.getMessage())
                .source(feedback.getSource())
                .isRead(feedback.getIsRead())
                .patientId(feedback.getPatient() != null ? feedback.getId() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
