package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.dto.response.ConsultantScheduleResponse;
import com.djio.grover_hospital.model.entity.ConsultantSchedule;
import com.djio.grover_hospital.repository.ConsultantScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultantScheduleService {

    private final ConsultantScheduleRepository scheduleRepository;

    public List<ConsultantScheduleResponse> getAllActive() {
        return scheduleRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(ConsultantScheduleResponse::from)
                .toList();
    }

    public List<ConsultantScheduleResponse> getByDepartment(Long departmentId) {
        return scheduleRepository.findByDepartmentIdAndIsActiveTrueOrderByDisplayOrderAsc(departmentId)
                .stream()
                .map(ConsultantScheduleResponse::from)
                .toList();
    }
}