package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.ConsultantSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantScheduleResponse {

    private Long id;
    private Long departmentId;
    private String departmentName;
    private String consultantName;
    private String scheduleText;
    private Integer displayOrder;

    public static ConsultantScheduleResponse from (ConsultantSchedule schedule) {
        return ConsultantScheduleResponse.builder()
                .id(schedule.getId())
                .departmentId(schedule.getDepartment().getId())
                .departmentName(schedule.getDepartment().getName())
                .consultantName(schedule.getConsultantName())
                .scheduleText(schedule.getScheduleText())
                .displayOrder(schedule.getDisplayOrder())
                .build();
    }
}
