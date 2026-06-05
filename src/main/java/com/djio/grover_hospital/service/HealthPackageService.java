package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.BulkCellsRequest;
import com.djio.grover_hospital.model.dto.request.HealthPackageRequest;
import com.djio.grover_hospital.model.dto.request.PackageInclusionRequest;
import com.djio.grover_hospital.model.dto.request.PackageTierRequest;
import com.djio.grover_hospital.model.dto.response.CellResponse;
import com.djio.grover_hospital.model.dto.response.HealthPackageResponse;
import com.djio.grover_hospital.model.dto.response.PackageInclusionResponse;
import com.djio.grover_hospital.model.dto.response.PackageTierResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.model.entity.HealthPackage;
import com.djio.grover_hospital.model.enums.InclusionStatus;
import com.djio.grover_hospital.model.entity.PackageInclusion;
import com.djio.grover_hospital.model.entity.PackageTier;
import com.djio.grover_hospital.model.entity.PackageTierInclusion;
import com.djio.grover_hospital.model.enums.Tone;
import com.djio.grover_hospital.repository.DepartmentRepository;
import com.djio.grover_hospital.repository.HealthPackageRepository;
import com.djio.grover_hospital.repository.PackageInclusionRepository;
import com.djio.grover_hospital.repository.PackageTierInclusionRepository;
import com.djio.grover_hospital.repository.PackageTierRepository;
import com.djio.grover_hospital.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthPackageService {

    private final HealthPackageRepository packageRepository;
    private final PackageTierRepository tierRepository;
    private final DepartmentRepository departmentRepository;
    private final PackageInclusionRepository inclusionRepository;
    private final PackageTierInclusionRepository cellRepository;


    // === Public read ===

    public List<HealthPackageResponse> getAllActive() {
        return packageRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponseWithMatrix)
                .toList();
    }

    public HealthPackageResponse getBySlug(String slug) {
        HealthPackage healthPackage = packageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "slug", slug));
        return toResponseWithMatrix(healthPackage);
    }

    // === Admin: Packages ===

    public List<HealthPackageResponse> getAllForAdmin() {
        return packageRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(this::toResponseWithMatrix)
                .toList();
    }

    public HealthPackageResponse getById(Long id) {
        HealthPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", id));
        return toResponseWithMatrix(pkg);
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
                .headingTone(request.getHeadingTone() != null ? request.getHeadingTone() : Tone.GREEN)
                .pricingTone(request.getPricingTone() != null ? request.getPricingTone() : Tone.GREEN)
                .build();

        return toResponseWithMatrix(packageRepository.save(healthPackage));
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
        if (request.getHeadingTone() != null) healthPackage.setHeadingTone(request.getHeadingTone());
        if (request.getPricingTone() != null) healthPackage.setPricingTone(request.getPricingTone());

        return toResponseWithMatrix(packageRepository.save(healthPackage));
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

    // === Admin: inclusions (rows of the matrix) ===

    @Transactional
    public PackageInclusionResponse addInclusion(Long packageId, PackageInclusionRequest request) {
        HealthPackage healthPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", packageId));

        PackageInclusion inclusion = PackageInclusion.builder()
                .healthPackage(healthPackage)
                .label(request.getLabel())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        return PackageInclusionResponse.from(inclusionRepository.save(inclusion));
    }

    @Transactional
    public PackageInclusionResponse updateInclusion(Long inclusionId, PackageInclusionRequest request) {
        PackageInclusion inclusion = inclusionRepository.findById(inclusionId)
                .orElseThrow(() -> new ResourceNotFoundException("Package inclusion", "id", inclusionId));

        inclusion.setLabel(request.getLabel());
        inclusion.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) inclusion.setDisplayOrder(request.getDisplayOrder());

        return PackageInclusionResponse.from(inclusionRepository.save(inclusion));
    }

    @Transactional
    public void deleteInclusion(Long inclusionId) {
        if (!inclusionRepository.existsById(inclusionId)) {
            throw new ResourceNotFoundException("Package inclusion", "id", inclusionId);
        }
        // FK cascade on package_tier_inclusions.inclusion_id removes any cells referencing this inclusion.
        inclusionRepository.deleteById(inclusionId);
    }

    // === Admin: bulk-replace cells (atomic full-grid write) ===

    /**
     * Atomically replace every cell belonging to {@code packageId} with the cells in {@code request}.
     * Deletes the existing rows first, then inserts the new set. Runs in a single transaction.
     *
     * <p>Validation:</p>
     * <ul>
     *   <li>Each {@code tierId} must belong to the target package.</li>
     *   <li>Each {@code inclusionId} must belong to the target package.</li>
     *   <li>Each (tierId, inclusionId) pair must appear at most once in the request.</li>
     *   <li>{@code note} must be non-null iff {@code status == CONDITIONAL}.</li>
     * </ul>
     */
    @Transactional
    public HealthPackageResponse bulkReplaceCells(Long packageId, BulkCellsRequest request) {
        HealthPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", packageId));

        Map<Long, PackageTier> tiersById = pkg.getTiers().stream()
                .collect(Collectors.toMap(PackageTier::getId, t -> t));
        Map<Long, PackageInclusion> inclusionsById = pkg.getInclusions().stream()
                .collect(Collectors.toMap(PackageInclusion::getId, i -> i));

        Set<String> seenPairs = new HashSet<>();
        List<PackageTierInclusion> toInsert = new ArrayList<>(request.getCells().size());

        for (BulkCellsRequest.Cell c : request.getCells()) {
            PackageTier tier = tiersById.get(c.getTierId());
            if (tier == null) {
                throw new BadRequestException(
                        "Tier id " + c.getTierId() + " does not belong to package " + packageId);
            }
            PackageInclusion inclusion = inclusionsById.get(c.getInclusionId());
            if (inclusion == null) {
                throw new BadRequestException(
                        "Inclusion id " + c.getInclusionId() + " does not belong to package " + packageId);
            }

            String pairKey = c.getTierId() + ":" + c.getInclusionId();
            if (!seenPairs.add(pairKey)) {
                throw new BadRequestException(
                        "Duplicate cell for tier " + c.getTierId() + " and inclusion " + c.getInclusionId());
            }

            validateNoteForStatus(c.getStatus(), c.getNote());

            toInsert.add(PackageTierInclusion.builder()
                    .tier(tier)
                    .inclusion(inclusion)
                    .status(c.getStatus())
                    .note(c.getStatus() == InclusionStatus.CONDITIONAL ? c.getNote() : null)
                    .build());
        }

        // Wipe and reinsert. The @Modifying delete uses flushAutomatically so any pending inserts
        // are pushed to the DB before the DELETE runs; the persistence context is NOT cleared so
        // the tier and inclusion entities referenced by the new cells stay managed for saveAll.
        cellRepository.deleteAllByPackageId(packageId);
        cellRepository.saveAll(toInsert);

        return toResponseWithMatrix(pkg);
    }

    // === Helpers ===

    /**
     * Build a HealthPackageResponse with its full matrix loaded and densified.
     * Cells that have no row in the DB are emitted as EXCLUDED so the frontend never sees a gap.
     */
    private HealthPackageResponse toResponseWithMatrix(HealthPackage pkg) {
        List<PackageTier> tiers = pkg.getTiers();
        List<PackageInclusion> inclusions = pkg.getInclusions();

        // Index real cells by "<tierId>:<inclusionId>" for O(1) lookup.
        List<PackageTierInclusion> rawCells = cellRepository.findAllByPackageId(pkg.getId());
        Map<String, PackageTierInclusion> cellsByKey = rawCells.stream()
                .collect(Collectors.toMap(
                        c -> c.getTier().getId() + ":" + c.getInclusion().getId(),
                        c -> c));

        List<CellResponse> densified = new ArrayList<>(tiers.size() * inclusions.size());
        for (PackageTier tier : tiers) {
            for (PackageInclusion inclusion : inclusions) {
                String key = tier.getId() + ":" + inclusion.getId();
                PackageTierInclusion existing = cellsByKey.get(key);
                densified.add(existing != null
                        ? CellResponse.from(existing)
                        : CellResponse.defaultExcluded(tier.getId(), inclusion.getId()));
            }
        }

        return HealthPackageResponse.from(pkg, densified);
    }

    private void validateNoteForStatus(InclusionStatus status, String note) {
        boolean hasNote = note != null && !note.isBlank();
        if (status == InclusionStatus.CONDITIONAL && !hasNote) {
            throw new BadRequestException("note is required when status is CONDITIONAL");
        }
        if (status != InclusionStatus.CONDITIONAL && hasNote) {
            throw new BadRequestException(
                    "note must be omitted when status is " + status + " (got: \"" + note + "\")");
        }
    }

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