package com.djio.grover_hospital.notification.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WhatsappMessage {
    private final String toPhoneNumber;
    private final String text;
    private final String templateName;
    private final List<String> templateParams;   // <-- was Map<String, String>, now List<String>
}