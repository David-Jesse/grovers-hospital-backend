package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.core.NotificationProviderException;
import com.djio.grover_hospital.notification.core.SendResult;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendchampSmsSenderIntegrationTest {

    private WireMockServer wireMock;
    private SendchampProperties properties;
    private SendchampSmsSender sender;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        properties = properties(wireMock.baseUrl());
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void sendsDocumentedSmsRequestWithBearerAuthentication() {
        wireMock.stubFor(post(urlEqualTo("/sms/send"))
                .withHeader("Authorization", equalTo("Bearer test-access-key"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("""
                        {"to":["2348012345678"],"message":"Test message","sender_name":"Grovers","route":"non_dnd"}
                        """))
                .willReturn(okJson("{}")));

        SendResult result = sender.send(SmsMessage.builder()
                .toPhoneNumber("0801 234 5678")
                .text("Test message")
                .build());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderMessageId()).isNull();
        wireMock.verify(postRequestedFor(urlEqualTo("/sms/send")));
    }

    @Test
    void omitsRouteWhenConfigurationIsBlank() {
        properties.setSmsRoute("   ");
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());
        stubSuccessfulRequestWithBody("""
                {"to":["2348012345678"],"message":"Test message","sender_name":"Grovers"}
                """);

        sendTestMessage();

        wireMock.verify(postRequestedFor(urlEqualTo("/sms/send"))
                .withRequestBody(notMatching(".*\\\"route\\\".*")));
    }

    @Test
    void serializesNonDndRoute() {
        properties.setSmsRoute("non_dnd");
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());
        stubSuccessfulRequestWithBody("""
                {"to":["2348012345678"],"message":"Test message","sender_name":"Grovers","route":"non_dnd"}
                """);

        sendTestMessage();
    }

    @Test
    void serializesDndRoute() {
        properties.setSmsRoute("dnd");
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());
        stubSuccessfulRequestWithBody("""
                {"to":["2348012345678"],"message":"Test message","sender_name":"Grovers","route":"dnd"}
                """);

        sendTestMessage();
    }

    @Test
    void rejectsUnsupportedRouteDuringConfigurationValidation() {
        properties.setSmsRoute("priority");

        assertThatThrownBy(() -> new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer()))
                .isInstanceOf(NotificationProviderException.class)
                .extracting(error -> ((NotificationProviderException) error).getReason())
                .isEqualTo(NotificationProviderException.Reason.CONFIGURATION);

        wireMock.verify(0, postRequestedFor(urlEqualTo("/sms/send")));
    }

    @Test
    void rejectsInvalidPhoneNumberBeforeCallingProvider() {
        assertThatThrownBy(() -> sender.send(SmsMessage.builder()
                .toPhoneNumber("+447700900123")
                .text("Test message")
                .build()))
                .isInstanceOf(NotificationProviderException.class)
                .extracting(error -> ((NotificationProviderException) error).getReason())
                .isEqualTo(NotificationProviderException.Reason.VALIDATION);

        wireMock.verify(0, postRequestedFor(urlEqualTo("/sms/send")));
    }

    @Test
    void mapsAuthenticationErrorsWithoutLeakingProviderResponse() {
        wireMock.stubFor(post(urlEqualTo("/sms/send")).willReturn(aResponse().withStatus(401)
                .withBody("sensitive provider response")));

        assertFailure(NotificationProviderException.Reason.AUTHENTICATION, 401);
    }

    @Test
    void mapsValidationErrorsWithoutLeakingProviderResponse() {
        wireMock.stubFor(post(urlEqualTo("/sms/send")).willReturn(aResponse().withStatus(400)
                .withBody("sensitive provider response")));

        assertFailure(NotificationProviderException.Reason.PROVIDER_REJECTED, 400);
    }

    @Test
    void mapsRateLimitsWithoutRetrying() {
        wireMock.stubFor(post(urlEqualTo("/sms/send")).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "1")));

        assertFailure(NotificationProviderException.Reason.RATE_LIMITED, 429);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/sms/send")));
    }

    @Test
    void mapsProviderServerErrorsWithoutRetrying() {
        wireMock.stubFor(post(urlEqualTo("/sms/send")).willReturn(serverError()));

        assertFailure(NotificationProviderException.Reason.PROVIDER_UNAVAILABLE, 500);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/sms/send")));
    }

    @Test
    void mapsReadTimeout() {
        properties.setReadTimeoutMs(50);
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());
        wireMock.stubFor(post(urlEqualTo("/sms/send"))
                .willReturn(okJson("{}").withFixedDelay(300)));

        assertFailure(NotificationProviderException.Reason.READ_TIMEOUT, null);
    }

    @Test
    void mapsConnectionFailure() {
        int stoppedPort = wireMock.port();
        wireMock.stop();
        properties.setBaseUrl("http://127.0.0.1:" + stoppedPort);
        sender = new SendchampSmsSender(properties, new SendchampPhoneNumberNormalizer());

        assertFailure(NotificationProviderException.Reason.CONNECTION_FAILURE, null);
    }

    @Test
    void mapsMalformedProviderResponse() {
        wireMock.stubFor(post(urlEqualTo("/sms/send")).willReturn(ok("not-json")));

        assertFailure(NotificationProviderException.Reason.MALFORMED_RESPONSE, null);
    }

    private void assertFailure(NotificationProviderException.Reason expectedReason, Integer expectedStatus) {
        assertThatThrownBy(() -> sender.send(SmsMessage.builder()
                .toPhoneNumber("08012345678")
                .text("Test message")
                .build()))
                .isInstanceOfSatisfying(NotificationProviderException.class, error -> {
                    assertThat(error.getReason()).isEqualTo(expectedReason);
                    assertThat(error.getStatusCode()).isEqualTo(expectedStatus);
                    assertThat(error.getMessage()).doesNotContain("sensitive", "2348012345678", "Test message", "test-access-key");
                });
    }

    private void stubSuccessfulRequestWithBody(String body) {
        wireMock.stubFor(post(urlEqualTo("/sms/send"))
                .withRequestBody(equalToJson(body))
                .willReturn(okJson("{}")));
    }

    private SendResult sendTestMessage() {
        return sender.send(SmsMessage.builder()
                .toPhoneNumber("0801 234 5678")
                .text("Test message")
                .build());
    }

    private static SendchampProperties properties(String baseUrl) {
        SendchampProperties properties = new SendchampProperties();
        properties.setBaseUrl(baseUrl);
        properties.setAccessKey("test-access-key");
        properties.setSmsSenderId("Grovers");
        properties.setSmsRoute("non_dnd");
        properties.setEmailSenderName("Grover Hospital");
        properties.setEmailSenderAddress("sender@example.test");
        properties.setConnectTimeoutMs(1_000);
        properties.setReadTimeoutMs(5_000);
        return properties;
    }
}
