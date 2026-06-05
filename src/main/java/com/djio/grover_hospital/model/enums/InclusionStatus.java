package com.djio.grover_hospital.model.enums;

/**
 * Status of a single cell in the package inclusion matrix.
 * <ul>
 *   <li>{@code INCLUDED}   — solid tick on the public page.</li>
 *   <li>{@code EXCLUDED}   — solid cross on the public page. Default for cells that have not been set.</li>
 *   <li>{@code CONDITIONAL}— note replaces the tick, e.g. "Every 2 years", "Limited". Requires a non-null note.</li>
 * </ul>
 * The {@code note} non-null-iff-CONDITIONAL invariant is enforced both at the service layer and via a CHECK constraint.
 */
public enum InclusionStatus {
    INCLUDED,
    EXCLUDED,
    CONDITIONAL
}