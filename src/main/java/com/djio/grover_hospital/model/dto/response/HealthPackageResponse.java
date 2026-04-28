package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.HealthPackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthPackageResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String inclusions;
    /** Null when the package has no fixed price*/
    private BigDecimal price;
    private Long departmentId;
    private String departmentName;
    private Integer displayOrder;

    public static HealthPackageResponse from(HealthPackage healthPackage) {
        return HealthPackageResponse.builder()
                .id(healthPackage.getId())
                .name(healthPackage.getName())
                .slug(healthPackage.getSlug())
                .description(healthPackage.getDescription())
                .inclusions(healthPackage.getInclusions())
                .price(healthPackage.getPrice())
                .departmentId(healthPackage.getDepartment() != null ? healthPackage.getDepartment().getId() : null)
                .departmentName(healthPackage.getDepartment() != null ? healthPackage.getDepartment().getName() : null)
                .displayOrder(healthPackage.getDisplayOrder())
                .build();
    }
}