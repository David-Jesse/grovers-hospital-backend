package com.djio.grover_hospital.model.enums;

public enum ResultStatus {
    /** Admin has started preparing the result but hasn't completed upload yet */
    PENDING,
    /** Result is fully uploaded and visible to the patient */
    AVAILABLE
}
