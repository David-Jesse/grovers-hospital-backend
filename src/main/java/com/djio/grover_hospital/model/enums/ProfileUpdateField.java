package com.djio.grover_hospital.model.enums;

/**
 * Health-profile fields a patient may submit an update request for.
 * These are the admin-only clinical fields patients can't self-edit,
 * plus OTHER for anything not covered (handled manually by admin).
 */
public enum ProfileUpdateField {
    BLOOD_GROUP,
    GENOTYPE,
    ALLERGIES,
    OTHER
}