package com.djio.grover_hospital.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the app.notification.events.* properties from application.properties
 * so per-patient notification_preferences can default to the same initial state.
 * <p>
 * Property names match what's already in application.properties, e.g.
 * app.notification.events.booking-confirmation.email=true
 */

@Configuration
@ConfigurationProperties(prefix = "app.notification.events")
@Getter
@Setter
public class NotificationDefaultProperties {

    private ChannelToggles bookingConfirmation = new ChannelToggles(true, false, true);
    private ChannelToggles bookingStatusUpdate = new ChannelToggles(true, true, true);
    private ChannelToggles resultReady = new ChannelToggles(true, true, false);

    @Getter
    @Setter
    public static class ChannelToggles {
        private boolean email;
        private boolean sms;
        private boolean whatsapp;

        public ChannelToggles() {
        }

        public ChannelToggles(boolean email, boolean sms, boolean whatsapp) {
            this.email = email;
            this.sms = sms;
            this.whatsapp = whatsapp;
        }
    }
}