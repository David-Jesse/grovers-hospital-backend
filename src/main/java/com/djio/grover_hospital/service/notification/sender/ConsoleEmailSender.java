package com.djio.grover_hospital.service.notification.sender;

import com.djio.grover_hospital.service.notification.channel.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notification.email.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleEmailSender implements EmailSender {

    private static final String SEPARATOR = "═══════════════════════════════════════════════════════════════════════════";

    @Override
    public void send(EmailMessage message) {
        StringBuilder out = new StringBuilder("\n").append(SEPARATOR).append("\n");
        out.append("📧  EMAIL (console)\n").append(SEPARATOR).append("\n");
        out.append("To:        ").append(message.getTo()).append("\n");
        if (message.getCc() != null && !message.getCc().isEmpty()) {
            out.append("Cc:        ").append(String.join(", ", message.getCc())).append("\n");
        }
        if (message.getBcc() != null && !message.getBcc().isEmpty()) {
            out.append("Bcc:       ").append(String.join(", ", message.getBcc())).append("\n");
        }
        out.append("Subject:   ").append(message.getSubject()).append("\n");
        out.append(SEPARATOR).append("\n");

        if (message.getTextBody() != null && !message.getTextBody().isBlank()) {
            out.append(message.getTextBody()).append("\n");
        } else if (message.getHtmlBody() != null) {
            out.append("[HTML body — ").append(message.getHtmlBody().length()).append(" chars]\n");
        }
        out.append(SEPARATOR).append("\n");

        log.info(out.toString());
    }
}