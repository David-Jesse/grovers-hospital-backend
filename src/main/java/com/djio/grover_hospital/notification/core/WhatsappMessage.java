package com.djio.grover_hospital.notification.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WhatsappMessage {
    private final String toMsisdn;
    private final String body;
}