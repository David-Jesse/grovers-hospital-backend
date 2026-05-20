package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import com.djio.grover_hospital.model.enums.PreferredContactMethod;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientFeedbackResponse {

    private Long id;
    private String subject;
    private String message;
    private FeedbackType type;
    private Short rating;
    private Boolean responseWanted;
    private PreferredContactMethod preferredContactMethod;
    private FeedbackStatus status;
    private OffsetDateTime createdAt;

    public static PatientFeedbackResponse from(Feedback f) {
        return PatientFeedbackResponse.builder()
                .id(f.getId())
                .subject(f.getSubject())
                .message(f.getMessage())
                .type(f.getType())
                .rating(f.getRating())
                .responseWanted(f.getResponseWanted())
                .preferredContactMethod(f.getPreferredContactMethod())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
