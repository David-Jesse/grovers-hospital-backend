package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.core.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notification.email.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleEmailSender implements EmailSender {

    @Override
    public SendResult send(EmailMessage message) {
        log.info("Console email submission accepted (delivery suppressed)");
        return SendResult.success("console-" + java.util.UUID.randomUUID());
    }
}
