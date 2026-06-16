package com.djio.grover_hospital.notification.yournotify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.notification.yournotify")
public class YourNotifyProperties {

    /** Base URL — https://api.yournotify.com */
    private String baseUrl = "https://api.yournotify.com";

    /** Bearer token from YourNotify Dashboard > Developer > API Keys. */
    private String apiKey;

    /**
     * Sender ID shown to recipients (the "from" field). Max 11 alphanumeric chars,
     * NCC-approved in Nigeria before live traffic.
     */
    private String senderId = "GROVERS";

    /** TCP connect timeout. */
    private int connectTimeoutMs = 10_000;

    /** HTTP read timeout — YourNotify accepts the request quickly; 30s is generous. */
    private int readTimeoutMs = 30_000;
}