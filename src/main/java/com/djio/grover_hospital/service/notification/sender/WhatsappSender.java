package com.djio.grover_hospital.service.notification.sender;

import com.djio.grover_hospital.service.notification.channel.WhatsappMessage;

public interface WhatsappSender {
    void send(WhatsappMessage message);
}