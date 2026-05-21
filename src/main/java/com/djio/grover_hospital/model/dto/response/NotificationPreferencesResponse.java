package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.NotificationPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {

    private Long id;

    private Boolean bookingConfirmationEmail;
    private Boolean bookingConfirmationSms;
    private Boolean bookingConfirmationWhatsapp;

    private Boolean bookingStatusUpdateEmail;
    private Boolean bookingStatusUpdateSms;
    private Boolean bookingStatusUpdateWhatsapp;

    private Boolean resultReadyEmail;
    private Boolean resultReadySms;
    private Boolean resultReadyWhatsapp;

    private OffsetDateTime updatedAt;

    public static NotificationPreferencesResponse from(NotificationPreference p) {
        return NotificationPreferencesResponse.builder()
                .id(p.getId())
                .bookingConfirmationEmail(p.getBookingConfirmationEmail())
                .bookingConfirmationSms(p.getBookingConfirmationSms())
                .bookingConfirmationWhatsapp(p.getBookingConfirmationWhatsapp())
                .bookingStatusUpdateEmail(p.getBookingStatusUpdateEmail())
                .bookingStatusUpdateSms(p.getBookingStatusUpdateSms())
                .bookingStatusUpdateWhatsapp(p.getBookingStatusUpdateWhatsapp())
                .resultReadyEmail(p.getResultReadyEmail())
                .resultReadySms(p.getResultReadySms())
                .resultReadyWhatsapp(p.getResultReadyWhatsapp())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}