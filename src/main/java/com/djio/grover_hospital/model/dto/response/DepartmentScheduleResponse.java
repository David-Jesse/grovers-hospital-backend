package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.DepartmentSchedule;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentScheduleResponse {

    private Long id;
    private Long departmentId;
    private String departmentName;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    public static DepartmentScheduleResponse from(DepartmentSchedule entity) {
        return DepartmentScheduleResponse.builder()
                .id(entity.getId())
                .departmentId(entity.getDepartment().getId())
                .departmentName(entity.getDepartment().getName())
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .build();
    }

    public static List<DepartmentScheduleResponse> fromList(List<DepartmentSchedule> entities) {
        return entities.stream().map(DepartmentScheduleResponse::from).collect(Collectors.toList());
    }
}