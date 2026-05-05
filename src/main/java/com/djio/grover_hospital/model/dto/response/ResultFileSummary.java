package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.ResultFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ResultFileSummary {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;

    public static ResultFileSummary from(ResultFile file) {
        return ResultFileSummary.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .build();
    }
}
