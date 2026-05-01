package com.djio.grover_hospital.service.notification.sender;

import com.djio.grover_hospital.service.notification.channel.EmailMessage;

public interface EmailSender {
    void send(EmailMessage message);
}