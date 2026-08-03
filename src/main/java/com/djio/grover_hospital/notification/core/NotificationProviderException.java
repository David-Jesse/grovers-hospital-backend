package com.djio.grover_hospital.notification.core;

/**
 * A safe, provider-facing failure. Its message is suitable for application logs and delivery
 * records: it never contains request contents, recipient details, or credentials.
 */
public class NotificationProviderException extends RuntimeException {

    public enum Reason {
        VALIDATION,
        AUTHENTICATION,
        RATE_LIMITED,
        PROVIDER_REJECTED,
        PROVIDER_UNAVAILABLE,
        CONNECTION_FAILURE,
        READ_TIMEOUT,
        MALFORMED_RESPONSE,
        UNSUPPORTED_FEATURE,
        CONFIGURATION
    }

    private final String provider;
    private final Reason reason;
    private final Integer statusCode;

    public NotificationProviderException(String provider, Reason reason, Integer statusCode, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.reason = reason;
        this.statusCode = statusCode;
    }

    public NotificationProviderException(String provider, Reason reason, String message) {
        this(provider, reason, null, message, null);
    }

    public String getProvider() {
        return provider;
    }

    public Reason getReason() {
        return reason;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
