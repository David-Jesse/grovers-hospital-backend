package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.HealthPackageRequest;
import com.djio.grover_hospital.model.dto.request.PackageTierRequest;
import com.djio.grover_hospital.model.dto.response.HealthPackageResponse;
import com.djio.grover_hospital.model.dto.response.PackageTierResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.model.entity.HealthPackage;
import com.djio.grover_hospital.model.entity.PackageTier;
import com.djio.grover_hospital.repository.DepartmentRepository;
import com.djio.grover_hospital.repository.HealthPackageRepository;
import com.djio.grover_hospital.repository.PackageTierRepository;
import com.djio.grover_hospital.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthPackageService {

    private final HealthPackageRepository packageRepository;
    private final PackageTierRepository tierRepository;
    private final DepartmentRepository departmentRepository;


    // === Public read ===

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

    // === Admin: Packages ===

    public List<HealthPackageResponse> getAllForAdmin() {
        return packageRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(HealthPackageResponse::from)
                .toList();
    }

    @Transactional
    public HealthPackageResponse createPackage(HealthPackageRequest request) {
        String slug = generateUniqueSlug(request.getName(), null);
        Department department = resolveDepartment(request.getDepartmentId());

        HealthPackage healthPackage = HealthPackage.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .targetAudience(request.getTargetAudience())
                .department(department)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return HealthPackageResponse.from(packageRepository.save(healthPackage));
    }

    @Transactional
    public HealthPackageResponse updatePackage(Long id, HealthPackageRequest request) {
        HealthPackage healthPackage = packageRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Package", "id", id));

        if (!healthPackage.getName().equals(request.getName())) {
            healthPackage.setSlug(generateUniqueSlug(request.getName(), id));
        }

        healthPackage.setName(request.getName());
        healthPackage.setDescription(request.getDescription());
        healthPackage.setTargetAudience(request.getTargetAudience());
        healthPackage.setDepartment(resolveDepartment(request.getDepartmentId()));
        if (request.getDisplayOrder() != null) healthPackage.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) healthPackage.setIsActive(request.getIsActive());

        return HealthPackageResponse.from(packageRepository.save(healthPackage));
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package", "id", id);
        }
        packageRepository.deleteById(id);
    }

    // === Admin: tiers ===

    @Transactional
    public PackageTierResponse addTier(Long packageId, PackageTierRequest request) {
        HealthPackage healthPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", packageId));

        PackageTier tier = PackageTier.builder()
                .healthPackage(healthPackage)
                .name(request.getName())
                .inclusions(request.getInclusions())
                .priceMale(request.getPriceMale())
                .priceFemale(request.getPriceFemale())
                .notes(request.getNotes())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return PackageTierResponse.from(tierRepository.save(tier));
    }

    @Transactional
    public PackageTierResponse updateTier(Long tierId, PackageTierRequest request) {
        PackageTier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Package tier", "id", tierId));

        tier.setName(request.getName());
        tier.setInclusions(request.getInclusions());
        tier.setPriceMale(request.getPriceMale());
        tier.setPriceFemale(request.getPriceFemale());
        tier.setNotes(request.getNotes());
        if (request.getDisplayOrder() != null) tier.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) tier.setIsActive(request.getIsActive());

        return PackageTierResponse.from(tierRepository.save(tier));
    }

    @Transactional
    public void deleteTier(Long tierId) {
        if (!tierRepository.existsById(tierId)) {
            throw new ResourceNotFoundException("Package tier", "id", tierId);
        }

        tierRepository.deleteById(tierId);
    }

    // === Helpers ===

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String baseSlug = SlugUtils.toSlug(name);
        if (baseSlug.isEmpty()) {
            throw new BadRequestException("Package name produces an invalid slug");
        }

        String slug = baseSlug;
        int counter = 1;
        while (slugExists(slug, excludeId)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private boolean slugExists(String slug, Long excludeId) {
        return packageRepository.findBySlug(slug)
                .filter(p -> !p.getId().equals(excludeId))
                .isPresent();
    }
}