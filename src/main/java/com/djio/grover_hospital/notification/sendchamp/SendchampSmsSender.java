package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.core.NotificationProviderException;
import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.notification.sender.SmsSender;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampApiResponse;
import com.djio.grover_hospital.notification.sendchamp.dto.SendchampSmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;

/** Sends transactional SMS with Sendchamp's documented REST request shape. */
@Slf4j
@Component
@EnableConfigurationProperties(SendchampProperties.class)
@ConditionalOnProperty(name = "app.notification.sms.provider", havingValue = "sendchamp")
public class SendchampSmsSender implements SmsSender {

    private final SendchampProperties properties;
    private final SendchampPhoneNumberNormalizer phoneNumberNormalizer;
    private final RestClient restClient;

    public SendchampSmsSender(SendchampProperties properties,
                              SendchampPhoneNumberNormalizer phoneNumberNormalizer) {
        validateConfiguration(properties);
        this.properties = properties;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(properties))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public SendResult send(SmsMessage message) {
        if (message == null || message.getText() == null || message.getText().isBlank()) {
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.VALIDATION,
                    "Sendchamp SMS requires a non-empty message");
        }

        String recipient = phoneNumberNormalizer.normalize(message.getToPhoneNumber());
        SendchampSmsRequest request = new SendchampSmsRequest(
                List.of(recipient), message.getText(), properties.getSmsSenderId(), configuredRoute());

        try {
            SendchampApiResponse response = restClient.post()
                    .uri("/sms/send")
                    .body(request)
                    .retrieve()
                    .body(SendchampApiResponse.class);
            if (response == null) {
                throw new NotificationProviderException("sendchamp",
                        NotificationProviderException.Reason.MALFORMED_RESPONSE,
                        "Sendchamp returned an empty SMS response");
            }

            // The response reference is intentionally null until Sendchamp supplies its response schema.
            log.info("Sendchamp accepted an SMS submission");
            return SendResult.success(null);
        } catch (NotificationProviderException e) {
            throw e;
        } catch (RestClientResponseException e) {
            throw responseFailure(e);
        } catch (ResourceAccessException e) {
            throw transportFailure(e);
        } catch (RestClientException e) {
            if (causedBySocketTimeout(e)) {
                throw new NotificationProviderException("sendchamp",
                        NotificationProviderException.Reason.READ_TIMEOUT,
                        null,
                        "Sendchamp SMS request timed out while waiting for a response", null);
            }
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.MALFORMED_RESPONSE,
                    null,
                    "Sendchamp returned an unreadable SMS response", null);
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(SendchampProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return factory;
    }

    private static void validateConfiguration(SendchampProperties properties) {
        if (properties == null || isBlank(properties.getBaseUrl()) || isBlank(properties.getAccessKey())
                || isBlank(properties.getSmsSenderId())
                || (!isBlank(properties.getSmsRoute())
                && !("dnd".equals(properties.getSmsRoute())
                || "non_dnd".equals(properties.getSmsRoute())
                || "international".equals(properties.getSmsRoute())))) {
            throw new NotificationProviderException("sendchamp",
                    NotificationProviderException.Reason.CONFIGURATION,
                    "Sendchamp SMS sender configuration is incomplete or invalid");
        }
    }

    private String configuredRoute() {
        return isBlank(properties.getSmsRoute()) ? null : properties.getSmsRoute();
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
                "Sendchamp SMS request failed with HTTP " + status, null);
    }

    private static NotificationProviderException transportFailure(ResourceAccessException e) {
        NotificationProviderException.Reason reason = causedBySocketTimeout(e)
                ? NotificationProviderException.Reason.READ_TIMEOUT
                : NotificationProviderException.Reason.CONNECTION_FAILURE;
        String message = reason == NotificationProviderException.Reason.READ_TIMEOUT
                ? "Sendchamp SMS request timed out while waiting for a response"
                : "Could not connect to Sendchamp SMS service";
        return new NotificationProviderException("sendchamp", reason, null, message, null);
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
}
