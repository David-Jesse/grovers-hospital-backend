package com.djio.grover_hospital.notification.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SmsMessage {
    /** Recipient MSISDN in international format (e.g. 2348012345678, no plus sign for SMPP). */
    private final String toMsisdn;
    private final String body;
}