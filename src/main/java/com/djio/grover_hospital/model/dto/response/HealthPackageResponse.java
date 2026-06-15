package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.HealthPackage;
import com.djio.grover_hospital.model.enums.Tone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Full single-package payload. Returned by:
 * <ul>
 *   <li>{@code GET /packages}            (public list)</li>
 *   <li>{@code GET /packages/{slug}}     (public detail)</li>
 *   <li>{@code GET /admin/packages}      (admin list)</li>
 *   <li>{@code GET /admin/packages/{id}} (admin detail)</li>
 * </ul>
 *
 * <p>The {@link #cells} list is densified — every (tier, inclusion) pair appears exactly once,
 * with status defaulting to {@code EXCLUDED} for pairs that have no row in the DB. The frontend
 * can therefore render the grid without checking for gaps.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthPackageResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String targetAudience;
    private Long departmentId;
    private String departmentName;
    private Integer displayOrder;
    private String headline;
    private Boolean isActive;

    private Tone headingTone;
    private Tone pricingTone;
    private String pricingNote;

    private List<PackageTierResponse> tiers;
    private List<PackageInclusionResponse> inclusions;
    private List<CellResponse> cells;

    /**
     * Build a response without cells — used by callers that don't need the matrix loaded
     * (currently none in this batch, but kept for future minimal-payload endpoints).
     */
    public static HealthPackageResponse from(HealthPackage pkg) {
        return from(pkg, Collections.emptyList());
    }

    /**
     * Canonical builder. Pass the densified cell list (see service layer) for the public/admin
     * endpoints that render the matrix.
     */
    public static HealthPackageResponse from(HealthPackage pkg, List<CellResponse> cells) {
        return HealthPackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .slug(pkg.getSlug())
                .description(pkg.getDescription())
                .targetAudience(pkg.getTargetAudience())
                .departmentId(pkg.getDepartment() != null ? pkg.getDepartment().getId() : null)
                .departmentName(pkg.getDepartment() != null ? pkg.getDepartment().getName() : null)
                .displayOrder(pkg.getDisplayOrder())
                .isActive(pkg.getIsActive())
                .headline(pkg.getHeadline())
                .pricingNote(pkg.getPricingNote())
                .headingTone(pkg.getHeadingTone())
                .pricingTone(pkg.getPricingTone())
                .tiers(pkg.getTiers() == null
                        ? Collections.emptyList()
                        : pkg.getTiers().stream().map(PackageTierResponse::from).toList())
                .inclusions(pkg.getInclusions() == null
                        ? Collections.emptyList()
                        : pkg.getInclusions().stream().map(PackageInclusionResponse::from).toList())
                .cells(cells)
                .build();
    }
}