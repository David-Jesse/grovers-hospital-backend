package com.djio.grover_hospital.notification.core;

public interface SmsSender {
    SendResult send(SmsMessage message);
}