package com.djio.grover_hospital.service.notification;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.Patient;

/**
 * High-level notification service used by business Logic
 * Each Method represents a business event and internally decides
 * which channels to dispatch through

 * Business code never thinks about channels directly - it just
 * fires the event and trusts the notification layer to handle delivery
 */

public interface NotificationService {

    void notifyBookingConfirmationToPatient(Booking booking);

    void notifyBookingAlertToHospital(Booking booking);

    void notifyBookingStatusUpdateToPatient(Booking booking);

    void notifyResultBody(Patient patient, String resultTitle);
}
