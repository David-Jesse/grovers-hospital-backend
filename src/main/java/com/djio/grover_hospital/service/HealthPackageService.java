package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.response.HealthPackageResponse;
import com.djio.grover_hospital.model.entity.HealthPackage;
import com.djio.grover_hospital.repository.HealthPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthPackageService {

    private final HealthPackageRepository packageRepository;

    public List<HealthPackageResponse> getAllActive() {
        return packageRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(HealthPackageResponse::from)
                .toList();
    }

    public HealthPackageResponse getBySlug(String slug) {
        HealthPackage healthPackage = packageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "slug", slug));
        return HealthPackageResponse.from(healthPackage);
    }
}