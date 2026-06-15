package com.djio.grover_hospital.notification.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/**
 * Fully-rendered email ready for the wire. Caller (EmailTemplateService) has already
 * resolved Thymeleaf templates into htmlBody/textBody.
 */
@Getter
@Builder
@AllArgsConstructor
public class EmailMessage {
    private final String to;
    @Singular private final List<String> ccs;   // exposed as getCcs()/getCc() below
    @Singular private final List<String> bccs;
    private final String subject;
    private final String htmlBody;
    private final String textBody;

    // Aliases so existing code using getCc()/getBcc() keeps working.
    public List<String> getCc()  { return ccs; }
    public List<String> getBcc() { return bccs; }
}