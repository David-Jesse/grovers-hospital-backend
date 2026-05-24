package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.MedicalDocument;
import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.model.enums.UploaderType;
import lombok.*;


import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocumentResponse {

    private Long id;
    private Long patientId;
    private UploaderType uploaderType;
    private String uploadedByName;
    private DocumentCategory category;
    private String title;
    private String description;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private OffsetDateTime createdAt;

    public static MedicalDocumentResponse from(MedicalDocument d) {
        String uploaderName = null;
        if (d.getUploaderType() == UploaderType.PATIENT && d.getUploadedByPatient() != null) {
            uploaderName = d.getUploadedByPatient().getFirstName() + " " + d.getUploadedByPatient().getLastName();
        } else if (d.getUploaderType() == UploaderType.ADMIN && d.getUploadedByAdmin() != null) {
            uploaderName = d.getUploadedByAdmin().getFullName();
        }

        return MedicalDocumentResponse.builder()
                .id(d.getId())
                .patientId(d.getPatient() != null ? d.getPatient().getId() : null)
                .uploaderType(d.getUploaderType())
                .uploadedByName(uploaderName)
                .category(d.getCategory())
                .title(d.getTitle())
                .description(d.getDescription())
                .originalFileName(d.getOriginalFileName())
                .contentType(d.getContentType())
                .fileSize(d.getFileSize())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
