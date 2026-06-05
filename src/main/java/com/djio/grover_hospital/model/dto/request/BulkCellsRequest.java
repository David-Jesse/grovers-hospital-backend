package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.InclusionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Payload for {@code PUT /admin/packages/{packageId}/cells} — the bulk-replace endpoint
 * that takes the entire grid in one request and atomically replaces every cell for the package.
 *
 * <p>Cells not present in the list are removed; the service does NOT default them to EXCLUDED on
 * write. The defaulting happens on read so that the admin UI is in complete control of what is sent.</p>
 */
@Data
public class BulkCellsRequest {

    @NotEmpty(message = "At least one cell is required")
    @Valid
    private List<Cell> cells;

    @Data
    public static class Cell {

        @NotNull(message = "tierId is required")
        private Long tierId;

        @NotNull(message = "inclusionId is required")
        private Long inclusionId;

        @NotNull(message = "status is required")
        private InclusionStatus status;

        /** Required iff status == CONDITIONAL. Service-layer validation enforces this. */
        @Size(max = 500)
        private String note;
    }
}