package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.DataExportJob;
import com.djio.grover_hospital.model.enums.DataExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportJobResponse {

    private Long id;
    private DataExportStatus status;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public static DataExportJobResponse from(DataExportJob j) {
        return DataExportJobResponse.builder()
                .id(j.getId())
                .status(j.getStatus())
                .expiresAt(j.getExpiresAt())
                .createdAt(j.getCreatedAt())
                .build();
    }
}