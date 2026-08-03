package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.WhatsappMessage;
import com.djio.grover_hospital.notification.core.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notification.whatsapp.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleWhatsappSender implements WhatsappSender {

    @Override
    public SendResult send(WhatsappMessage message) {
        log.info("Console WhatsApp submission accepted (delivery suppressed)");
        return SendResult.success("console-" + System.currentTimeMillis());
    }
}
