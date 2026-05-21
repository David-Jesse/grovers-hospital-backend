package com.djio.grover_hospital.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * All fields optional. Only non-null values are applied — null leaves
 * the existing pref untouched. Send the whole object with all toggles
 * for a complete replacement, or a subset to patch individual toggles.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesRequest {

    private Boolean bookingConfirmationEmail;
    private Boolean bookingConfirmationSms;
    private Boolean bookingConfirmationWhatsapp;

    private Boolean bookingStatusUpdateEmail;
    private Boolean bookingStatusUpdateSms;
    private Boolean bookingStatusUpdateWhatsapp;

    private Boolean resultReadyEmail;
    private Boolean resultReadySms;
    private Boolean resultReadyWhatsapp;
}