package com.djio.grover_hospital.service.notification.sender;

import com.djio.grover_hospital.service.notification.channel.WhatsappMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notification.whatsapp.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleWhatsappSender implements WhatsappSender {

    private static final String SEPARATOR = "─────────────────────────────────────────────";

    @Override
    public void send(WhatsappMessage message) {
        StringBuilder out = new StringBuilder("\n").append(SEPARATOR).append("\n");
        out.append("📱  WHATSAPP (console)\n").append(SEPARATOR).append("\n");
        out.append("To:        ").append(message.getToPhoneNumber()).append("\n");

        if (message.getTemplateName() != null) {
            out.append("Template:  ").append(message.getTemplateName()).append("\n");
            if (message.getTemplateParams() != null && !message.getTemplateParams().isEmpty()) {
                out.append("Params:    ").append(message.getTemplateParams()).append("\n");
            }
        }

        if (message.getText() != null && !message.getText().isBlank()) {
            out.append("Text:\n").append(message.getText()).append("\n");
        }
        out.append(SEPARATOR).append("\n");

        log.info(out.toString());
    }
}