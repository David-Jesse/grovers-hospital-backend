package com.djio.grover_hospital.util;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.ChronicCondition;
import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.entity.HealthProfile;
import com.djio.grover_hospital.model.entity.Medication;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.entity.Result;
import com.djio.grover_hospital.model.entity.Visit;
import com.djio.grover_hospital.repository.BookingRepository;
import com.djio.grover_hospital.repository.ChronicConditionRepository;
import com.djio.grover_hospital.repository.FeedbackRepository;
import com.djio.grover_hospital.repository.HealthProfileRepository;
import com.djio.grover_hospital.repository.MedicationRepository;
import com.djio.grover_hospital.repository.NotificationPreferenceRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.repository.ResultRepository;
import com.djio.grover_hospital.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads every patient-owned table into a single nested Map ready for JSON
 * serialization.

 * Designed to be the "personal data dump" the patient receives via the data
 * export endpoint. Includes both administrative records (bookings, visits)
 * and clinical records (medications, conditions, lab result metadata).

 * Lab result file blobs themselves are NOT included — they're stored as
 * AES-encrypted bytes on disk and exporting them raw isn't useful. Only the
 * metadata (title, date, file name, size) is included so the patient knows
 * which results exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataExportCollector {

    private final PatientRepository patientRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicationRepository medicationRepository;
    private final ChronicConditionRepository chronicConditionRepository;
    private final VisitRepository visitRepository;
    private final BookingRepository bookingRepository;
    private final ResultRepository resultRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> collectAllDataFor(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + patientId));

        Map<String, Object> dump = new LinkedHashMap<>();
        dump.put("exportedAt", OffsetDateTime.now().toString());
        dump.put("exportedFor", "Grover's Hospital Patient Data Export");

        dump.put("profile", toProfileMap(patient));
        dump.put("healthProfile", toHealthProfileMap(patientId));
        dump.put("notificationPreferences", toNotificationPrefsMap(patientId));
        dump.put("medications", toMedicationsList(patientId));
        dump.put("chronicConditions", toChronicConditionsList(patientId));
        dump.put("visits", toVisitsList(patientId));
        dump.put("bookings", toBookingsList(patientId));
        dump.put("results", toResultsList(patientId));
        dump.put("feedback", toFeedbackList(patientId));

        return dump;
    }

    // ============================================================
    // Per-section builders. Each returns a List/Map of plain objects.
    // ============================================================

    private Map<String, Object> toProfileMap(Patient p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("firstName", p.getFirstName());
        m.put("lastName", p.getLastName());
        m.put("email", p.getEmail());
        m.put("phone", p.getPhone());
        m.put("whatsappNumber", p.getWhatsappNumber());
        m.put("dateOfBirth", p.getDateOfBirth());
        m.put("gender", p.getGender() != null ? p.getGender() + p.getFirstName() + " " + p.getLastName() : null);
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> toHealthProfileMap(Long patientId) {
        return healthProfileRepository.findByPatientId(patientId)
                .map(this::healthProfileToMap)
                .orElse(null);
    }

    private Map<String, Object> healthProfileToMap(HealthProfile h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bloodGroup", h.getBloodGroup());
        m.put("genotype", h.getGenotype());
        m.put("allergies", h.getAllergies());
        m.put("clinicalNotes", h.getClinicalNotes());
        m.put("heightCm", h.getHeightCm());
        m.put("weightKg", h.getWeightKg());
        m.put("emergencyContactName", h.getEmergencyContactName());
        m.put("emergencyContactRelationship", h.getEmergencyContactRelationship());
        m.put("emergencyContactPhone", h.getEmergencyContactPhone());
        m.put("updatedAt", h.getUpdatedAt());
        return m;
    }

    private Map<String, Object> toNotificationPrefsMap(Long patientId) {
        return notificationPreferenceRepository.findByPatientId(patientId)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bookingConfirmationEmail", p.getBookingConfirmationEmail());
                    m.put("bookingConfirmationSms", p.getBookingConfirmationSms());
                    m.put("bookingConfirmationWhatsapp", p.getBookingConfirmationWhatsapp());
                    m.put("bookingStatusUpdateEmail", p.getBookingStatusUpdateEmail());
                    m.put("bookingStatusUpdateSms", p.getBookingStatusUpdateSms());
                    m.put("bookingStatusUpdateWhatsapp", p.getBookingStatusUpdateWhatsapp());
                    m.put("resultReadyEmail", p.getResultReadyEmail());
                    m.put("resultReadySms", p.getResultReadySms());
                    m.put("resultReadyWhatsapp", p.getResultReadyWhatsapp());
                    return m;
                })
                .orElse(null);
    }

    private List<Map<String, Object>> toMedicationsList(Long patientId) {
        return medicationRepository.findByPatientIdOrderByIsActiveDescStartDateDesc(patientId)
                .stream().map(this::medicationToMap).toList();
    }

    private Map<String, Object> medicationToMap(Medication med) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", med.getId());
        m.put("name", med.getName());
        m.put("dosage", med.getDosage());
        m.put("frequency", med.getFrequency());
        m.put("startDate", med.getStartDate());
        m.put("endDate", med.getEndDate());
        m.put("isActive", med.getIsActive());
        m.put("notes", med.getNotes());
        m.put("prescribedBy", med.getPrescribedBy() != null
                ? med.getPrescribedBy().getFullName()
                : med.getPrescribedByText());
        m.put("createdAt", med.getCreatedAt());
        return m;
    }

    private List<Map<String, Object>> toChronicConditionsList(Long patientId) {
        return chronicConditionRepository.findByPatientIdOrderByDiagnosedDateDesc(patientId)
                .stream().map(this::conditionToMap).toList();
    }

    private Map<String, Object> conditionToMap(ChronicCondition c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("diagnosedDate", c.getDiagnosedDate());
        m.put("status", c.getStatus() != null ? c.getStatus().name() : null);
        m.put("notes", c.getNotes());
        m.put("managingDoctor", c.getManagingDoctor() != null
                ? c.getManagingDoctor().getFullName()
                : c.getManagingDoctorText());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private List<Map<String, Object>> toVisitsList(Long patientId) {
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId)
                .stream().map(this::visitToMap).toList();
    }

    private Map<String, Object> visitToMap(Visit v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("bookingId", v.getBooking() != null ? v.getBooking().getId() : null);
        m.put("visitDate", v.getVisitDate());
        m.put("chiefComplaint", v.getChiefComplaint());
        m.put("diagnosis", v.getDiagnosis());
        m.put("treatment", v.getTreatment());
        m.put("clinicalNotes", v.getClinicalNotes());
        m.put("followUpRequired", v.getFollowUpRequired());
        m.put("followUpDate", v.getFollowUpDate());
        m.put("attendingDoctor", v.getAttendingDoctor() != null
                ? v.getAttendingDoctor().getFullName()
                : v.getAttendingDoctorText());
        m.put("createdAt", v.getCreatedAt());
        return m;
    }

    private List<Map<String, Object>> toBookingsList(Long patientId) {
        return bookingRepository.findByPatientIdOrderByCreatedAtDesc(patientId, Pageable.unpaged())
                .getContent()
                .stream().map(this::bookingToMap).toList();
    }

    private Map<String, Object> bookingToMap(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("type", b.getBookingType() != null ? b.getBookingType().name() : null);
        m.put("status", b.getStatus() != null ? b.getStatus().name() : null);
        m.put("preferredDate", b.getPreferredDate());
        m.put("department", b.getDepartment() != null ? b.getDepartment().getName() : null);
        m.put("healthPackage", b.getHealthPackage() != null ? b.getHealthPackage().getName() : null);
        m.put("packageTier", b.getPackageTier() != null ? b.getPackageTier().getName() : null);
        m.put("notes", b.getNotes());
        m.put("adminNotes", b.getAdminNotes());
        m.put("createdAt", b.getCreatedAt());
        return m;
    }

    private List<Map<String, Object>> toResultsList(Long patientId) {
        return resultRepository.findByPatientIdOrderByCreatedAtDesc(patientId, Pageable.unpaged())
                .getContent()
                .stream().map(this::resultToMap).toList();
    }

    private Map<String, Object> resultToMap(Result r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("title", r.getTitle());
        m.put("notes", r.getDescription());
        m.put("createdAt", r.getCreatedAt());
        // We deliberately omit the encrypted file contents.
        m.put("_note", "Encrypted file contents are not included in this export. " +
                "Log into the patient portal to view/download individual result files.");
        return m;
    }

    private List<Map<String, Object>> toFeedbackList(Long patientId) {
        return feedbackRepository.findByPatientIdAndSourceOrderByCreatedAtDesc(
                        patientId, com.djio.grover_hospital.model.enums.FeedbackSource.PORTAL)
                .stream().map(this::feedbackToMap).toList();
    }

    private Map<String, Object> feedbackToMap(Feedback f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("subject", f.getSubject());
        m.put("message", f.getMessage());
        m.put("type", f.getType() != null ? f.getType().name() : null);
        m.put("rating", f.getRating());
        m.put("responseWanted", f.getResponseWanted());
        m.put("status", f.getStatus() != null ? f.getStatus().name() : null);
        m.put("createdAt", f.getCreatedAt());
        return m;
    }
}