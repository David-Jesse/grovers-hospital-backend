package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.core.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notification.sms.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleSmsSender implements SmsSender {

    private static final String SEPARATOR = "─────────────────────────────────────────────";

    @Override
    public SendResult send(SmsMessage message) {
        StringBuilder out = new StringBuilder("\n").append(SEPARATOR).append("\n");
        out.append("💬  SMS (console)\n").append(SEPARATOR).append("\n");
        out.append("To:    ").append(message.getToPhoneNumber()).append("\n");
        out.append("Text:  ").append(message.getText()).append("\n");
        out.append(SEPARATOR).append("\n");

        log.info(out.toString());
        return SendResult.success("console-" + java.util.UUID.randomUUID());
    }
}