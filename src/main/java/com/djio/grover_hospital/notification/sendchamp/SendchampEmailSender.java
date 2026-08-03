package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.core.NotificationProviderException;
import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.notification.sender.EmailSender;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailAddress;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailBody;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailRequest;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;

/**
 * Sends transactional email using Sendchamp's documented JSON request shape.
 */
@Slf4j
@Component
@EnableConfigurationProperties(SendchampProperties.class)
@ConditionalOnProperty(name = "app.notification.email.provider", havingValue = "sendchamp")
public class SendchampEmailSender implements EmailSender {

    private static final String EMAIL_CONTENT_TYPE = "text/html";
    private static final String DEFAULT_RECIPIENT_NAME = "Recipient";

    private final SendchampProperties properties;
    private final RestClient restClient;

    public SendchampEmailSender(SendchampProperties properties) {
        validateConfiguration(properties);
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(properties))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public SendResult send(EmailMessage message) {
        if (message == null) {
            throw validationFailure("Sendchamp email requires a message");
        }
        if ((message.getCc() != null && !message.getCc().isEmpty())
                || (message.getBcc() != null && !message.getBcc().isEmpty())) {
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.UNSUPPORTED_FEATURE,
                    "Sendchamp email adapter does not support CC or BCC");
        }
        if (isBlank(message.getTo()) || isBlank(message.getSubject())) {
            throw validationFailure("Sendchamp email requires recipient and subject");
        }

        SendchampEmailRequest request = createRequest(message);

        try {
            // An empty successful body is valid. Unknown JSON response fields are ignored.
            ResponseEntity<SendchampEmailResponse> response = restClient.post()
                    .uri("/email/send")
                    .body(request)
                    .retrieve()
                    .toEntity(SendchampEmailResponse.class);

            SendchampEmailResponse responseBody = response.getBody();
            validateSuccessfulResponse(responseBody, response.getStatusCode().value());
            log.info("Sendchamp accepted an email submission with HTTP {}", response.getStatusCode().value());
            return SendResult.success(responseBody != null && responseBody.data() != null
                    ? responseBody.data().id()
                    : null);
        } catch (RestClientResponseException e) {
            throw responseFailure(e);
        } catch (ResourceAccessException e) {
            throw transportFailure(e);
        } catch (RestClientException e) {
            if (causedBySocketTimeout(e)) {
                throw new NotificationProviderException("sendchamp",
                        NotificationProviderException.Reason.READ_TIMEOUT, null,
                        "Sendchamp email request timed out while waiting for a response", null);
            }
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.MALFORMED_RESPONSE, null,
                    "Sendchamp returned an unreadable email response", null);
        }
    }

    /**
     * Builds the exact object handed to Spring's production JSON message converter.
     * Package visibility permits serialization-contract tests without sending a request.
     */
    SendchampEmailRequest createRequest(EmailMessage message) {
        String body = !isBlank(message.getHtmlBody()) ? message.getHtmlBody() : message.getTextBody();
        if (isBlank(body)) {
            throw validationFailure("Sendchamp email requires an HTML or plain-text body");
        }
        return new SendchampEmailRequest(
                message.getSubject(),
                List.of(new SendchampEmailAddress(message.getTo(), recipientName(message.getRecipientName()))),
                properties.isIncludeEmailSender()
                        ? new SendchampEmailAddress(properties.getEmailSenderAddress(), properties.getEmailSenderName())
                        : null,
                new SendchampEmailBody(EMAIL_CONTENT_TYPE, body)
        );
    }

    private static void validateConfiguration(SendchampProperties properties) {
        if (properties == null || isBlank(properties.getBaseUrl()) || isBlank(properties.getAccessKey())
                || (properties.isIncludeEmailSender()
                && (isBlank(properties.getEmailSenderAddress()) || isBlank(properties.getEmailSenderName())))) {
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.CONFIGURATION,
                    "Sendchamp email sender configuration is incomplete");
        }
    }

    private static void validateSuccessfulResponse(SendchampEmailResponse response, int httpStatus) {
        if (response == null) {
            return;
        }
        boolean rejected = response.status() != null && !"success".equalsIgnoreCase(response.status());
        rejected |= response.code() != null && response.code() != 200;
        rejected |= response.data() != null && response.data().status() != null
                && !"sent".equalsIgnoreCase(response.data().status());
        if (rejected) {
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.PROVIDER_REJECTED, httpStatus,
                    "Sendchamp rejected the email submission", null);
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(SendchampProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return factory;
    }

    private static NotificationProviderException responseFailure(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        NotificationProviderException.Reason reason = switch (status) {
            case 401, 403 -> NotificationProviderException.Reason.AUTHENTICATION;
            case 429 -> NotificationProviderException.Reason.RATE_LIMITED;
            default -> status >= 500
                    ? NotificationProviderException.Reason.PROVIDER_UNAVAILABLE
                    : NotificationProviderException.Reason.PROVIDER_REJECTED;
        };
        return new NotificationProviderException("sendchamp", reason, status,
                "Sendchamp email request failed with HTTP " + status, null);
    }

    private static NotificationProviderException transportFailure(ResourceAccessException e) {
        NotificationProviderException.Reason reason = causedBySocketTimeout(e)
                ? NotificationProviderException.Reason.READ_TIMEOUT
                : NotificationProviderException.Reason.CONNECTION_FAILURE;
        String message = reason == NotificationProviderException.Reason.READ_TIMEOUT
                ? "Sendchamp email request timed out while waiting for a response"
                : "Could not connect to Sendchamp email service";
        return new NotificationProviderException("sendchamp", reason, null, message, null);
    }

    private static NotificationProviderException validationFailure(String message) {
        return new NotificationProviderException("sendchamp",
                NotificationProviderException.Reason.VALIDATION, message);
    }

    private static boolean causedBySocketTimeout(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String recipientName(String value) {
        return isBlank(value) ? DEFAULT_RECIPIENT_NAME : value.trim();
    }
}
