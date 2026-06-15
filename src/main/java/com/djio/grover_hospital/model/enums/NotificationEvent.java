package com.djio.grover_hospital.model.enums;

/**
 * Canonical notification events fired by the application.
 * Each value maps to a Thymeleaf email template under templates/email/{eventName}.html
 * (lowercase, kebab-case form of the enum) and a corresponding SMS copy.
 */
public enum NotificationEvent {
    BOOKING_CONFIRMED,
    BOOKING_STATUS_CHANGED,
    BOOKING_RESCHEDULED,
    APPOINTMENT_REMINDER,
    RESULT_READY,
    PASSWORD_RESET,
    FEEDBACK_RECEIVED,
    DATA_EXPORT_READY,
    ACCOUNT_DELETION_SCHEDULED;

    /** Returns the template basename (e.g. BOOKING_CONFIRMED -> "booking-confirmed"). */
    public String templateName() {
        return name().toLowerCase().replace('_', '-');
    }
}