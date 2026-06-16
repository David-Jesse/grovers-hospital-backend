package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.WhatsappMessage;
import com.djio.grover_hospital.notification.core.SendResult;

public interface WhatsappSender {
    SendResult send(WhatsappMessage message);
}