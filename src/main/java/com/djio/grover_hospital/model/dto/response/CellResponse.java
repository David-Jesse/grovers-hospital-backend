package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.enums.InclusionStatus;
import com.djio.grover_hospital.model.entity.PackageTierInclusion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CellResponse {

    private Long tierId;
    private Long inclusionId;
    private InclusionStatus status;
    /** Non-null only when status == CONDITIONAL. */
    private String note;

    public static CellResponse from(PackageTierInclusion cell) {
        return CellResponse.builder()
                .tierId(cell.getTier().getId())
                .inclusionId(cell.getInclusion().getId())
                .status(cell.getStatus())
                .note(cell.getNote())
                .build();
    }

    /**
     * Build a default EXCLUDED cell for a tier/inclusion pair that has no row in the DB.
     * Used on read to densify the grid so the frontend never sees a gap.
     */
    public static CellResponse defaultExcluded(Long tierId, Long inclusionId) {
        return CellResponse.builder()
                .tierId(tierId)
                .inclusionId(inclusionId)
                .status(InclusionStatus.EXCLUDED)
                .build();
    }
}