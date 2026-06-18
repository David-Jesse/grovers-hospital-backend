package com.djio.grover_hospital.notification;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.Patient;

/**
 * High-level notification service used by business logic.
 * Each method represents a business event and internally decides
 * which channels (email, SMS, WhatsApp) to dispatch through.
 * <p>
 * Business code never thinks about channels directly — it just
 * fires the event and trusts the notification layer to handle delivery.
 */
public interface NotificationService {

    void notifyBookingConfirmationToPatient(Booking booking);

    void notifyBookingAlertToHospital(Booking booking);

    void notifyBookingStatusUpdateToPatient(Booking booking);

    void notifyResultReady(Patient patient, String resultTitle);

    void notifyPasswordResetLink(Patient patient, String resetToken);

    void notifyAppointmentReminderToPatient(Booking booking);

    void notifyResultDownloadLink(Patient patient, String resultTitle, String downloadUrl);

    /**
     * Send to hospital when new feedback arrives (homepage form or patient portal)
     */
    void notifyFeedbackReceived(Feedback feedback);
}