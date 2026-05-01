package com.djio.grover_hospital.service.notification.channel;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmailMessage {

    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String htmlBody;
    private String textBody;
}