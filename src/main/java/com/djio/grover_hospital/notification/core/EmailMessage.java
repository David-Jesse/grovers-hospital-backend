package com.djio.grover_hospital.notification.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Fully-rendered email ready for the wire. Caller (EmailTemplateService) has already
 * resolved Thymeleaf templates into htmlBody/textBody.
 */
@Getter
@Builder
@AllArgsConstructor
public class EmailMessage {
    private final String toAddress;
    private final String subject;
    private final String htmlBody;
    private final String textBody;
}