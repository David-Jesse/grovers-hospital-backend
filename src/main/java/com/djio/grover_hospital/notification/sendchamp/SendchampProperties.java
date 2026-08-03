package com.djio.grover_hospital.notification.sendchamp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Properties supplied by the Sendchamp dashboard through deployment environment variables. */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.notification.sendchamp")
public class SendchampProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String accessKey;

    private String smsSenderId;

    private String smsRoute;

    private String emailSenderName;

    private String emailSenderAddress;

    private boolean includeEmailSender = false;

    @Min(1)
    private int connectTimeoutMs = 10_000;

    @Min(1)
    private int readTimeoutMs = 30_000;
}
