package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.PackageInclusion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageInclusionResponse {

    private Long id;
    private String label;
    private String description;
    private Integer displayOrder;

    public static PackageInclusionResponse from(PackageInclusion inclusion) {
        return PackageInclusionResponse.builder()
                .id(inclusion.getId())
                .label(inclusion.getLabel())
                .description(inclusion.getDescription())
                .displayOrder(inclusion.getDisplayOrder())
                .build();
    }
}