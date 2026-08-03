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

    @Override
    public SendResult send(SmsMessage message) {
        log.info("Console SMS submission accepted (delivery suppressed)");
        return SendResult.success("console-" + java.util.UUID.randomUUID());
    }
}
