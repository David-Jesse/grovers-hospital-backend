package com.djio.grover_hospital.notification.yournotify;

import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.notification.sender.SmsSender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sends transactional SMS via YourNotify's REST API.
 *
 * <p>YourNotify models every send as a Campaign — for transactional one-off sends we create
 * a campaign with a single recipient and status="running" (immediate dispatch). The endpoint
 * returns a campaign_id which we store as the provider_message_id for later DLR correlation
 * via the sms.direct.completed webhook.</p>
 *
 * <p>Note on the "lists" field: despite the name, it accepts an inline array of phone numbers
 * — not list IDs. This is YourNotify's chosen schema for direct sends.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.notification.sms.provider", havingValue = "yournotify")
public class YourNotifySmsSender implements SmsSender {

    private final YourNotifyProperties properties;

    private RestClient restClient;

    @PostConstruct
    void initClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("YourNotify SMS sender ready (base={}, sender={})",
                properties.getBaseUrl(), properties.getSenderId());
    }

    @Override
    public SendResult send(SmsMessage message) {
        String recipient = normalizeMsisdn(message.getToPhoneNumber());
        if (recipient.isBlank()) {
            return SendResult.failure("Recipient phone number is blank");
        }

        // Body shape per YourNotify's example:
        //   { name, from, text, status: "running", lists: [phone...], channel: "sms" }
        Map<String, Object> body = Map.of(
                "name", "transactional-" + System.currentTimeMillis(),
                "from", properties.getSenderId(),
                "text", message.getText(),
                "status", "running",
                "lists", List.of(recipient),
                "channel", "sms"
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/campaigns/sms")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String campaignId = extractCampaignId(response);
            log.info("YourNotify SMS sent to={} campaignId={}", recipient, campaignId);
            return SendResult.success(campaignId);

        } catch (RestClientResponseException e) {
            // 4xx / 5xx with a body — log the response so we can debug field-name issues
            log.error("YourNotify SMS rejected for to={} status={} body={}",
                    recipient, e.getStatusCode(), e.getResponseBodyAsString());
            return SendResult.failure("HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("YourNotify SMS failed for to={}: {}", recipient, e.getMessage(), e);
            return SendResult.failure(e.getMessage());
        }
    }

    /**
     * YourNotify returns responses in their standard wrapper. Try a few common shapes to
     * extract whatever identifies the campaign — we'll store this as provider_message_id.
     */
    @SuppressWarnings("unchecked")
    private String extractCampaignId(Map<String, Object> response) {
        if (response == null) return null;

        // Most common: { status: "success", data: { id: 457, ... } }
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object id = ((Map<String, Object>) dataMap).get("id");
            if (id == null) id = ((Map<String, Object>) dataMap).get("campaign_id");
            if (id != null) return String.valueOf(id);
        }

        // Fallback: top-level id field
        Object id = response.get("id");
        if (id == null) id = response.get("campaign_id");
        return id != null ? String.valueOf(id) : null;
    }

    /**
     * Normalize to international format with leading +. Handles common Nigerian patterns:
     *   "+2348012345678" → "+2348012345678" (unchanged)
     *   "2348012345678"  → "+2348012345678"
     *   "08012345678"    → "+2348012345678"
     */
    private String normalizeMsisdn(String phone) {
        if (phone == null) return "";
        String digitsAndPlus = phone.replaceAll("[^0-9+]", "");
        if (digitsAndPlus.isEmpty()) return "";
        if (digitsAndPlus.startsWith("+")) return digitsAndPlus;
        String digits = digitsAndPlus.replaceAll("[^0-9]", "");
        if (digits.startsWith("234")) return "+" + digits;
        if (digits.startsWith("0") && digits.length() == 11) return "+234" + digits.substring(1);
        return "+" + digits;  // best-effort fallback
    }
}