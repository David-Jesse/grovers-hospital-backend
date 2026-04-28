package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.response.DepartmentResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getAllActive() {
        return departmentRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getBySlug(String slug) {
        Department department = departmentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "slug", slug));
        return DepartmentResponse.from(department);
    }
}
