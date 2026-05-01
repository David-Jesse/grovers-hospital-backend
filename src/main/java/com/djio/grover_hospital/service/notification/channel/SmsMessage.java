package com.djio.grover_hospital.service.notification.channel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsMessage {

    private String toPhoneNumber;
    private String text;
}