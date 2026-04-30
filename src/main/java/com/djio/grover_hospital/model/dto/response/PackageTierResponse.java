package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.PackageTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageTierResponse {

    private Long id;
    private String name;
    private String inclusions;
    /** null when on fixed price */
    private BigDecimal priceMale;
    private BigDecimal priceFemale;
    private String notes;
    private Integer displayOrder;

    public static PackageTierResponse from (PackageTier tier) {
        return PackageTierResponse.builder()
                .id(tier.getId())
                .name(tier.getName())
                .inclusions(tier.getInclusions())
                .priceMale(tier.getPriceMale())
                .priceFemale(tier.getPriceFemale())
                .notes(tier.getNotes())
                .displayOrder(tier.getDisplayOrder())
                .build();

    }
}
