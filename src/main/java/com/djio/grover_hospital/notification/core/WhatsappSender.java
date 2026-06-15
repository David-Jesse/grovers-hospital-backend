package com.djio.grover_hospital.notification.core;

public interface WhatsappSender {
    SendResult send(WhatsappMessage message);
}