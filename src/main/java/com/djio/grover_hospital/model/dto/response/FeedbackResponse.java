package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.model.enums.PreferredContactMethod;
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
    private FeedbackType type;
    private Short rating;
    private Boolean responseWanted;
    private PreferredContactMethod preferredContactMethod;
    private FeedbackStatus status;
    private Boolean isRead;
    private Long patientId;
    private OffsetDateTime createdAt;

    public static FeedbackResponse from(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .patientId(f.getPatient() != null ? f.getPatient().getId() : null)
                .name(f.getName())
                .email(f.getEmail())
                .subject(f.getSubject())
                .message(f.getMessage())
                .source(f.getSource())
                .isRead(f.getIsRead())
                .type(f.getType())
                .rating(f.getRating())
                .responseWanted(f.getResponseWanted())
                .preferredContactMethod(f.getPreferredContactMethod())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}