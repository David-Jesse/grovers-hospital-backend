package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.DepartmentRequest;
import com.djio.grover_hospital.model.dto.response.DepartmentResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.repository.DepartmentRepository;
import com.djio.grover_hospital.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    // ==== ADMIN ====

    public List<DepartmentResponse> getAllForAdmin() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getByIdForAdmin(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentResponse.from(department);
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String slug = generateUniqueSlug(request.getName(), null);

        Department department =  Department.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Re-generate slug only if the name actually changed
        if (!department.getName().equals(request.getName())) {
            department.setSlug(generateUniqueSlug(request.getName(), id));
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setIconUrl(request.getIconUrl());
        if (request.getDisplayOrder() != null) department.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) department.setIsActive(request.getIsActive());

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        departmentRepository.deleteById(id);
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String baseSlug = SlugUtils.toSlug(name);
        if (baseSlug.isEmpty()) {
            throw new BadRequestException("Department name produces an invalid slug");
        }

        String slug = baseSlug;
        int counter = 1;
        while (slugExists(slug, excludeId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private boolean slugExists(String slug, Long excludeId) {
        return departmentRepository.findBySlug(slug)
                .filter(d -> !d.getId().equals(excludeId))
                .isPresent();
    }
}
