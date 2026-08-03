package com.djio.grover_hospital.notification.smtp;

import com.djio.grover_hospital.notification.channel.EmailMessage;
import com.djio.grover_hospital.notification.sender.EmailSender;
import com.djio.grover_hospital.notification.core.SendResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.notification.email.provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email.from-address}")
    private String fromAddress;

    @Value("${app.notification.email.from-name:Grover's Hospital}")
    private String fromName;

    @Override
    public SendResult send(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(
                    message.getTextBody() != null ? message.getTextBody() : "",
                    message.getHtmlBody() != null ? message.getHtmlBody() : "");

            if (message.getCc() != null && !message.getCc().isEmpty()) {
                helper.setCc(message.getCc().toArray(new String[0]));
            }
            if (message.getBcc() != null && !message.getBcc().isEmpty()) {
                helper.setBcc(message.getBcc().toArray(new String[0]));
            }

            mailSender.send(mimeMessage);

            String messageId = mimeMessage.getMessageID();
            log.info("SMTP accepted an email submission; messageId={}", messageId);
            return SendResult.success(messageId);

        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("SMTP email submission failed; exceptionType={}", e.getClass().getSimpleName());
            return SendResult.failure("SMTP email submission failed");
        }
    }
}
