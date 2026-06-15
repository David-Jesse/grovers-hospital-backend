package com.djio.grover_hospital.notification.sender;

import com.djio.grover_hospital.notification.channel.WhatsappMessage;

public interface WhatsappSender {
    void send(WhatsappMessage message);
}