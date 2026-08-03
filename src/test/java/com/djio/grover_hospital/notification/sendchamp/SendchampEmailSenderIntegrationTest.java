package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.core.NotificationProviderException;
import com.djio.grover_hospital.notification.core.SendResult;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class SendchampEmailSenderIntegrationTest {

    private WireMockServer wireMock;
    private SendchampProperties properties;
    private SendchampEmailSender sender;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        properties = properties(wireMock.baseUrl());
        sender = new SendchampEmailSender(properties);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void mapsSuccessfulResponseAndExtractsProviderMessageIdWhileOmittingSenderByDefault() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .withHeader("Authorization", equalTo("Bearer test-access-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "subject":"Appointment confirmation",
                          "to":[{"email":"recipient@example.test","name":"Ada"}],
                          "message_body":{"type":"text/html","value":"<p>HTML body</p>"}
                        }
                        """))
                .willReturn(okJson("""
                        {
                          "code": 200,
                          "data": {
                            "id": "MN-EMAIL-ebc464bc-2e48-4156-a6a0-666e3d8e8361",
                            "subject": "Appointment confirmation",
                            "email": "recipient@example.test",
                            "status": "sent"
                          },
                          "errors": null,
                          "message": "emails sent",
                          "status": "success"
                        }
                        """)));

        SendResult result = sender.send(message("<p>HTML body</p>", "Plain text", "Ada"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderMessageId())
                .isEqualTo("MN-EMAIL-ebc464bc-2e48-4156-a6a0-666e3d8e8361");
        wireMock.verify(postRequestedFor(urlEqualTo("/email/send")));
    }

    @Test
    void includesConfiguredSenderWhenEnabled() {
        properties.setIncludeEmailSender(true);
        sender = new SendchampEmailSender(properties);
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .withRequestBody(matchingJsonPath("$.from.email", equalTo("clo@grovershospital.com")))
                .withRequestBody(matchingJsonPath("$.from.name", equalTo("Grovers Hospital")))
                .willReturn(okJson("{}")));

        sender.send(message("<p>HTML body</p>", null, null));

        wireMock.verify(postRequestedFor(urlEqualTo("/email/send"))
                .withRequestBody(matchingJsonPath("$.from")));
    }

    @Test
    void acceptsSuccessfulEmailResponseWithAnEmptyBody() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")));

        SendResult result = sender.send(message("<p>HTML body</p>", null, null));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderMessageId()).isNull();
    }

    @Test
    void usesPlainTextWhenHtmlIsUnavailableWhileKeepingDocumentedContentType() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .withRequestBody(matchingJsonPath("$.message_body.type", equalTo("text/html")))
                .withRequestBody(matchingJsonPath("$.message_body.value", equalTo("Plain text")))
                .willReturn(okJson("{}")));

        sender.send(message(null, "Plain text", null));
        wireMock.verify(postRequestedFor(urlEqualTo("/email/send")));
    }

    @Test
    void rejectsMissingSenderConfiguration() {
        properties.setIncludeEmailSender(true);
        properties.setEmailSenderAddress(" ");

        assertThatThrownBy(() -> new SendchampEmailSender(properties))
                .isInstanceOfSatisfying(NotificationProviderException.class, error -> {
                    assertThat(error.getReason()).isEqualTo(NotificationProviderException.Reason.CONFIGURATION);
                    assertThat(error.getMessage()).doesNotContain("test-access-key", "clo@grovershospital.com");
                });
    }

    @Test
    void rejectsCc() {
        assertThatThrownBy(() -> sender.send(EmailMessage.builder()
                .to("recipient@example.test")
                .cc("copy@example.test")
                .subject("Subject")
                .textBody("Message")
                .build()))
                .isInstanceOfSatisfying(NotificationProviderException.class,
                        error -> assertThat(error.getReason()).isEqualTo(NotificationProviderException.Reason.UNSUPPORTED_FEATURE));
    }

    @Test
    void rejectsBcc() {
        assertThatThrownBy(() -> sender.send(EmailMessage.builder()
                .to("recipient@example.test")
                .bcc("blind-copy@example.test")
                .subject("Subject")
                .textBody("Message")
                .build()))
                .isInstanceOfSatisfying(NotificationProviderException.class,
                        error -> assertThat(error.getReason()).isEqualTo(NotificationProviderException.Reason.UNSUPPORTED_FEATURE));
    }

    @Test
    void mapsNoActiveSubscriptionResponseWithoutLeakingProviderBody() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(aResponse().withStatus(400).withBody("no active subscription for recipient@example.test")));

        assertFailure(NotificationProviderException.Reason.PROVIDER_REJECTED, 400);
    }

    @Test
    void mapsAuthenticationFailures() {
        wireMock.stubFor(post(urlEqualTo("/email/send")).willReturn(aResponse().withStatus(401)));

        assertFailure(NotificationProviderException.Reason.AUTHENTICATION, 401);
    }

    @Test
    void mapsRateLimitsWithoutRetrying() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "1")));

        assertFailure(NotificationProviderException.Reason.RATE_LIMITED, 429);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/email/send")));
    }

    @Test
    void mapsProviderServerErrorsWithoutRetrying() {
        wireMock.stubFor(post(urlEqualTo("/email/send")).willReturn(serverError()));

        assertFailure(NotificationProviderException.Reason.PROVIDER_UNAVAILABLE, 500);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/email/send")));
    }

    @Test
    void mapsConnectionFailure() {
        int stoppedPort = wireMock.port();
        wireMock.stop();
        properties.setBaseUrl("http://127.0.0.1:" + stoppedPort);
        sender = new SendchampEmailSender(properties);

        assertFailure(NotificationProviderException.Reason.CONNECTION_FAILURE, null);
    }

    @Test
    void mapsReadTimeout() {
        properties.setReadTimeoutMs(50);
        sender = new SendchampEmailSender(properties);
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(okJson("{}").withFixedDelay(300)));

        assertFailure(NotificationProviderException.Reason.READ_TIMEOUT, null);
    }

    @Test
    void mapsMalformedSuccessfulResponse() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[")));

        assertFailure(NotificationProviderException.Reason.MALFORMED_RESPONSE, null);
    }

    @Test
    void rejectsProviderLevelFailureInsideHttp200Response() {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(okJson("""
                        {
                          "code": 200,
                          "data": {"status": "failed"},
                          "errors": {"email": ["recipient rejected"]},
                          "message": "email not sent",
                          "status": "success"
                        }
                        """)));

        assertFailure(NotificationProviderException.Reason.PROVIDER_REJECTED, 200);
    }

    @Test
    void doesNotLogSensitiveValues(CapturedOutput output) {
        wireMock.stubFor(post(urlEqualTo("/email/send"))
                .willReturn(okJson("""
                        {"code":200,"data":{"id":"provider-secret-id","status":"sent"},"status":"success"}
                        """)));

        sender.send(message("<p>sensitive-body</p>", "sensitive-text", "Sensitive Recipient"));

        assertThat(output).doesNotContain(
                "test-access-key",
                "recipient@example.test",
                "clo@grovershospital.com",
                "sensitive-body",
                "sensitive-text",
                "Sensitive Recipient",
                "provider-secret-id");
    }

    @Test
    void mapsEmptyNonSuccessResponse() {
        wireMock.stubFor(post(urlEqualTo("/email/send")).willReturn(aResponse().withStatus(400)));

        assertFailure(NotificationProviderException.Reason.PROVIDER_REJECTED, 400);
    }

    private void assertFailure(NotificationProviderException.Reason expectedReason, Integer expectedStatus) {
        assertThatThrownBy(() -> sender.send(message("<p>HTML body</p>", "Plain text", null)))
                .isInstanceOfSatisfying(NotificationProviderException.class, error -> {
                    assertThat(error.getReason()).isEqualTo(expectedReason);
                    assertThat(error.getStatusCode()).isEqualTo(expectedStatus);
                    assertThat(error.getMessage()).doesNotContain(
                            "recipient@example.test", "clo@grovershospital.com", "HTML body", "Plain text", "test-access-key");
                });
    }

    private static EmailMessage message(String htmlBody, String textBody, String recipientName) {
        return EmailMessage.builder()
                .to("recipient@example.test")
                .recipientName(recipientName)
                .subject("Appointment confirmation")
                .htmlBody(htmlBody)
                .textBody(textBody)
                .build();
    }

    private static SendchampProperties properties(String baseUrl) {
        SendchampProperties properties = new SendchampProperties();
        properties.setBaseUrl(baseUrl);
        properties.setAccessKey("test-access-key");
        properties.setSmsSenderId("Grovers");
        properties.setSmsRoute("non_dnd");
        properties.setEmailSenderName("Grovers Hospital");
        properties.setEmailSenderAddress("clo@grovershospital.com");
        properties.setConnectTimeoutMs(1_000);
        properties.setReadTimeoutMs(5_000);
        return properties;
    }
}
