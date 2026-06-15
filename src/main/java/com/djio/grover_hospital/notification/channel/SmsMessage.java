package com.djio.grover_hospital.notification.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SmsMessage {
    /** Recipient MSISDN, e.g. +2348012345678 or 2348012345678. */
    private final String toPhoneNumber;
    private final String text;
}