package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.notification.channel.EmailMessage;

public interface EmailSender {
    SendResult send(EmailMessage message);
}