package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.channel.EmailMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks the Sendchamp wire contract to the ObjectMapper configured by the application.
 * All addresses and content in this test are synthetic.
 */
@JsonTest
class SendchampEmailRequestSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeJsonTreeEqualsKnownWorkingDocumentedPayload() throws Exception {
        SendchampEmailSender sender = sender(false);
        EmailMessage message = EmailMessage.builder()
                .to("valid-address@gmail.com")
                .recipientName("David-Jesse")
                .subject("SendChamp API test")
                .htmlBody("<p>Controlled API test.</p>")
                .build();

        JsonNode actual = objectMapper.valueToTree(sender.createRequest(message));
        JsonNode documented = objectMapper.readTree("""
                {
                  "subject": "SendChamp API test",
                  "to": [
                    {
                      "email": "valid-address@gmail.com",
                      "name": "David-Jesse"
                    }
                  ],
                  "message_body": {
                    "type": "text/html",
                    "value": "<p>Controlled API test.</p>"
                  }
                }
                """);

        assertThat(actual).isEqualTo(documented);
        assertThat(actual.has("from")).isFalse();
        assertThat(actual.toString()).doesNotContain(
                "\"cc\"", "\"bcc\"", "\"htmlBody\"", "\"textBody\"",
                "\"recipient\"", "\"sender\"", ":null");
    }

    @Test
    void actualBookingEmailRequestHasTheDocumentedShape() {
        SendchampEmailSender sender = sender(false);
        EmailMessage bookingEmail = EmailMessage.builder()
                .to("booking-recipient@example.test")
                .subject("Booking Received - #123")
                .textBody("""
                        Your booking request has been received.
                        Booking reference: 123
                        """)
                .build();

        JsonNode actual = objectMapper.valueToTree(sender.createRequest(bookingEmail));

        assertThat(fieldNames(actual)).containsExactlyInAnyOrder("subject", "to", "message_body");
        assertThat(fieldNames(actual.path("to").get(0))).containsExactlyInAnyOrder("email", "name");
        assertThat(fieldNames(actual.path("message_body"))).containsExactlyInAnyOrder("type", "value");
        assertThat(actual.path("message_body").path("type").textValue()).isEqualTo("text/html");
        assertThat(actual.path("to").get(0).path("name").textValue()).isEqualTo("Recipient");
        assertThat(actual.toString()).doesNotContain(":null");
    }

    @Test
    void validRecipientNameIsPreserved() {
        assertThat(recipientName("David-Jesse")).isEqualTo("David-Jesse");
    }

    @Test
    void surroundingRecipientNameWhitespaceIsTrimmed() {
        assertThat(recipientName("  David-Jesse \t")).isEqualTo("David-Jesse");
    }

    @Test
    void nullRecipientNameUsesFallback() {
        assertThat(recipientName(null)).isEqualTo("Recipient");
    }

    @Test
    void emptyRecipientNameUsesFallback() {
        assertThat(recipientName("")).isEqualTo("Recipient");
    }

    @Test
    void blankRecipientNameUsesFallback() {
        assertThat(recipientName(" \t ")).isEqualTo("Recipient");
    }

    @Test
    void serializedJsonNeverContainsAnEmptyRecipientName() throws Exception {
        JsonNode actual = objectMapper.valueToTree(requestForName("  "));

        assertThat(actual.path("to").get(0).path("name").textValue()).isEqualTo("Recipient");
        assertThat(objectMapper.writeValueAsString(actual)).doesNotContain("\"name\":\"\"");
    }

    @Test
    void nestedOptionalPropertiesAreOmittedRatherThanSerializedAsNull() {
        JsonNode address = objectMapper.valueToTree(
                new com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailAddress(
                        "recipient@example.test", null));
        JsonNode body = objectMapper.valueToTree(
                new com.djio.grover_hospital.notification.sendchamp.dto.SendchampEmailBody(
                        "text/html", null));

        assertThat(address.has("name")).isFalse();
        assertThat(body.has("value")).isFalse();
    }

    private static Set<String> fieldNames(JsonNode node) {
        return new java.util.HashSet<>(node.propertyNames());
    }

    private String recipientName(String name) {
        return objectMapper.valueToTree(requestForName(name))
                .path("to").get(0).path("name").textValue();
    }

    private static Object requestForName(String name) {
        EmailMessage message = EmailMessage.builder()
                .to("recipient@example.test")
                .recipientName(name)
                .subject("Synthetic subject")
                .textBody("Synthetic body")
                .build();
        return sender(false).createRequest(message);
    }

    private static SendchampEmailSender sender(boolean includeSender) {
        SendchampProperties properties = new SendchampProperties();
        properties.setBaseUrl("http://127.0.0.1:1");
        properties.setAccessKey("synthetic-test-key");
        properties.setIncludeEmailSender(includeSender);
        return new SendchampEmailSender(properties);
    }
}
