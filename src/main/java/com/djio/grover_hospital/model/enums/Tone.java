package com.djio.grover_hospital.model.enums;

/**
 * Visual tone for a HealthPackage's heading and pricing emphasis on the public page.
 * Stored as VARCHAR in the DB with a CHECK constraint; frontend maps to its own theme tokens
 */

public enum Tone {
    GREEN,
    RED,
    BLUE,
    DARK
}
