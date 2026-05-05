package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Result;
import com.djio.grover_hospital.model.enums.ResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResultResponse {

    private Long id;
    private String title;
    private String description;
    private ResultStatus status;
    private Boolean isNotified;

    private Long patientId;
    private String patientName;
    private String patientEmail;

    private Long uploadedById;
    private String uploadedByName;

    private Long bookingId;

    private List<ResultFileSummary> files;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminResultResponse from(Result result) {
        return AdminResultResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .status(result.getStatus())
                .isNotified(result.getIsNotified())
                .patientId(result.getPatient().getId())
                .patientName(result.getPatient().getFirstName() + " " + result.getPatient().getLastName())
                .patientEmail(result.getPatient().getEmail())
                .uploadedById(result.getUploadedBy().getId())
                .uploadedByName(result.getUploadedBy().getFullName())
                .bookingId(result.getBooking() != null ? result.getBooking().getId() : null)
                .files(result.getFiles() == null ? List.of() : result.getFiles().stream().map(ResultFileSummary::from).toList())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
