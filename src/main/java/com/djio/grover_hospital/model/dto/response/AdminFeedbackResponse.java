package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.FeedbackSource;
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
public class AdminFeedbackResponse {

    private Long id;
    private FeedbackSource source;

    // Submitter context - for HOMEPAGE these come from name/email;
    private Long patientId;
    private String submitterName;
    private String submitterEmail;
    private String submitterPhone;

    private String subject;
    private String message;

    // Portal only fields
    private FeedbackType type;
    private Short rating;
    private Boolean responseWanted;
    private PreferredContactMethod preferredContactMethod;
    private FeedbackStatus status;

    private Boolean isRead;

    private Long reviewedByAdminId;
    private String reviewedByAdminName;
    private OffsetDateTime reviewedAt;

    private String adminInternalNotes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminFeedbackResponse from(Feedback f) {
        Patient p = f.getPatient();
        Admin reviewer = f.getReviewedByAdmin();

        // For PORTAL feedback, fall back to the patient's contact info
        String submitterName = p != null ? p.getFirstName() + " " + p.getLastName() : f.getName();
        String submitterEmail = p != null ? p.getEmail() : f.getEmail();
        String submitterPhone = p != null ? p.getPhone() : null;

        return AdminFeedbackResponse.builder()
                .id(f.getId())
                .source(f.getSource())
                .patientId(p != null ? p.getId() : null)
                .submitterName(submitterName)
                .submitterEmail(submitterEmail)
                .submitterPhone(submitterPhone)
                .subject(f.getSubject())
                .message(f.getMessage())
                .type(f.getType())
                .rating(f.getRating())
                .responseWanted(f.getResponseWanted())
                .preferredContactMethod(f.getPreferredContactMethod())
                .status(f.getStatus())
                .isRead(f.getIsRead())
                .reviewedByAdminId(reviewer != null ? reviewer.getId() : null)
                .reviewedByAdminName(reviewer != null ? reviewer.getFullName() : null)
                .reviewedAt(f.getReviewedAt())
                .adminInternalNotes(f.getAdminInternalNotes())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
