package com.djio.grover_hospital.service.notification.sender;

import com.djio.grover_hospital.service.notification.channel.SmsMessage;

public interface SmsSender {
    void send(SmsMessage message);
}
