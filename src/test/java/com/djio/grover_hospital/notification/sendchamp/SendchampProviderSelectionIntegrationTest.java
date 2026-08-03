package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.sender.ConsoleEmailSender;
import com.djio.grover_hospital.notification.sender.ConsoleSmsSender;
import com.djio.grover_hospital.notification.sender.EmailSender;
import com.djio.grover_hospital.notification.sender.SmsSender;
import com.djio.grover_hospital.notification.smtp.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class SendchampProviderSelectionIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SenderConfiguration.class);

    @Test
    void selectsExactlyTheConsoleSendersWhenConsoleIsConfigured() {
        contextRunner.withPropertyValues(
                "app.notification.sms.provider=console",
                "app.notification.email.provider=console")
                .run(context -> {
                    assertThat(context.getBeansOfType(SmsSender.class)).hasSize(1);
                    assertThat(context.getBean(SmsSender.class)).isInstanceOf(ConsoleSmsSender.class);
                    assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(ConsoleEmailSender.class);
                });
    }

    @Test
    void selectsExactlyTheSendchampSendersWhenSendchampIsConfigured() {
        contextRunner.withPropertyValues(
                "app.notification.sms.provider=sendchamp",
                "app.notification.email.provider=sendchamp",
                "app.notification.sendchamp.base-url=http://localhost:8089",
                "app.notification.sendchamp.access-key=test-key",
                "app.notification.sendchamp.sms-sender-id=Grovers",
                "app.notification.sendchamp.sms-route=non_dnd",
                "app.notification.sendchamp.email-sender-name=Grover Hospital",
                "app.notification.sendchamp.email-sender-address=sender@example.test")
                .run(context -> {
                    assertThat(context.getBeansOfType(SmsSender.class)).hasSize(1);
                    assertThat(context.getBean(SmsSender.class)).isInstanceOf(SendchampSmsSender.class);
                    assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SendchampEmailSender.class);
                });
    }

    @Test
    void selectsSendchampEmailWithoutRequiringSmsConfiguration() {
        contextRunner.withPropertyValues(
                "app.notification.sms.provider=console",
                "app.notification.email.provider=sendchamp",
                "app.notification.sendchamp.base-url=http://localhost:8089",
                "app.notification.sendchamp.access-key=test-key",
                "app.notification.sendchamp.email-sender-name=Grover Hospital",
                "app.notification.sendchamp.email-sender-address=sender@example.test")
                .run(context -> {
                    assertThat(context.getBean(SmsSender.class)).isInstanceOf(ConsoleSmsSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SendchampEmailSender.class);
                });
    }

    @Test
    void selectsSmtpOnlyWhenSmtpIsConfigured() {
        contextRunner.withPropertyValues(
                "app.notification.sms.provider=console",
                "app.notification.email.provider=smtp",
                "app.notification.email.from-address=sender@example.test")
                .run(context -> {
                    assertThat(context.getBeansOfType(SmsSender.class)).hasSize(1);
                    assertThat(context.getBean(SmsSender.class)).isInstanceOf(ConsoleSmsSender.class);
                    assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({
            ConsoleSmsSender.class,
            ConsoleEmailSender.class,
            SmtpEmailSender.class,
            SendchampSmsSender.class,
            SendchampEmailSender.class
    })
    static class SenderConfiguration {

        @Bean
        SendchampPhoneNumberNormalizer sendchampPhoneNumberNormalizer() {
            return new SendchampPhoneNumberNormalizer();
        }

        @Bean
        JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl();
        }
    }
}
