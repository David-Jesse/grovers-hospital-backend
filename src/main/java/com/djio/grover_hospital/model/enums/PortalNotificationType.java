package com.djio.grover_hospital.model.enums;

public enum PortalNotificationType {
    // Patient facing
    BOOKING_RECEIVED,
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    BOOKING_REMINDER,
    RESULT_READY,
    MEDICAL_HISTORY_UPDATED,
    FEEDBACK_RECEIVED,
    FEEDBACK_RESPONSE,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,

    // Admin-facing
    NEW_BOOKING_ALERT,
    NEW_FEEDBACK_ALERT,

    // Generic catch-all
    GENERAL
}
