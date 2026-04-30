package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.ConsultantScheduleRequest;
import com.djio.grover_hospital.model.dto.response.ConsultantScheduleResponse;
import com.djio.grover_hospital.model.entity.ConsultantSchedule;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.repository.ConsultantScheduleRepository;
import com.djio.grover_hospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultantScheduleService {

    private final ConsultantScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;

    // ==== Public Read ===

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

    // === Admin ===

    public List<ConsultantScheduleResponse> getAllForAdmin() {
        return scheduleRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(ConsultantScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ConsultantScheduleResponse create(ConsultantScheduleRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        ConsultantSchedule schedule = ConsultantSchedule.builder()
                .department(department)
                .consultantName(request.getConsultantName())
                .scheduleText(request.getScheduleText())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return ConsultantScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public ConsultantScheduleResponse update(Long id, ConsultantScheduleRequest request) {
        ConsultantSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant schedule", "id", id));

        // If department changed, re-fetch
        if (!schedule.getDepartment().getId().equals(request.getDepartmentId())) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            schedule.setDepartment(department);
        }

        schedule.setConsultantName(request.getConsultantName());
        schedule.setScheduleText(request.getScheduleText());
        if (request.getDisplayOrder() != null) schedule.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) schedule.setIsActive(request.getIsActive());

        return ConsultantScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consultant schedule", "id", id);
        }
        scheduleRepository.deleteById(id);
    }
}