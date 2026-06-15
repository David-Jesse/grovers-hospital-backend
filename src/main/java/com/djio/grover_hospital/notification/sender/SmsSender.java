package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.SmsMessage;
import com.djio.grover_hospital.notification.core.SendResult;

public interface SmsSender {
    SendResult send(SmsMessage message);
}
