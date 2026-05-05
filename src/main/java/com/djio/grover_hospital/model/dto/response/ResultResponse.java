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
public class ResultResponse {

    private Long id;
    private String title;
    private String description;
    private ResultStatus status;
    private Long bookingId;
    private List<ResultFileSummary> files;
    private OffsetDateTime createdAt;

    public static ResultResponse from(Result result) {
        return ResultResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .status(result.getStatus())
                .bookingId(result.getBooking() != null ? result.getBooking().getId() : null)
                .files(result.getFiles() == null ? List.of() : result.getFiles().stream().map(ResultFileSummary::from).toList())
                .createdAt(result.getCreatedAt())
                .build();

    }
}