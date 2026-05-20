package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.VisitRequest;
import com.djio.grover_hospital.model.dto.response.VisitResponse;
import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.model.entity.Visit;
import com.djio.grover_hospital.repository.DoctorRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.repository.VisitRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitService {

    private static final String RESOURCE_TYPE = "VISIT";

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    // ============================================================
    // Auto-stub creation (called from BookingService.updateStatus
    // when a booking flips to COMPLETED).
    // Idempotent: if a Visit already exists for this booking, no-op.
    // ============================================================

    @Transactional
    public Visit createStubForCompletedBooking(Booking booking) {
        if (visitRepository.existsByBookingId(booking.getId())) {
            log.debug("Visit already exists for booking {}, skipping stub creation", booking.getId());
            return visitRepository.findByBookingId(booking.getId()).orElseThrow();
        }

        Visit stub = Visit.builder()
                .patient(booking.getPatient())
                .booking(booking)
                .followUpRequired(false)
                .build();

        Visit saved = visitRepository.save(stub);
        log.info("Auto-created visit stub {} for completed booking {}", saved.getId(), booking.getId());
        return saved;
    }

    // ============================================================
    // Admin GET/PUT
    // ============================================================

    @Transactional(readOnly = true)
    public VisitResponse getById(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id " + visitId));
        return VisitResponse.from(visit);
    }

    @Transactional(readOnly = true)
    public VisitResponse getByBookingId(Long bookingId) {
        Visit visit = visitRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No visit found for booking id " + bookingId));
        return VisitResponse.from(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId)
                .stream().map(VisitResponse::from).toList();
    }

    @Transactional
    public VisitResponse update(Long visitId, VisitRequest request, HttpServletRequest httpRequest) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id " + visitId));

        if (request.getVisitDate() != null) {
            visit.setVisitDate(request.getVisitDate());
        }
        visit.setChiefComplaint(request.getChiefComplaint());
        visit.setDiagnosis(request.getDiagnosis());
        visit.setTreatment(request.getTreatment());
        visit.setClinicalNotes(request.getClinicalNotes());
        if (request.getFollowUpRequired() != null) {
            visit.setFollowUpRequired(request.getFollowUpRequired());
        }
        visit.setFollowUpDate(request.getFollowUpDate());
        visit.setAttendingDoctor(resolveDoctor(request.getAttendingDoctorId()));
        visit.setAttendingDoctorText(request.getAttendingDoctorText());

        Visit saved = visitRepository.save(visit);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "VISIT_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest);
        log.info("Admin {} updated visit {}", adminId, saved.getId());

        return VisitResponse.from(saved);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Doctor resolveDoctor(Long doctorId) {
        if (doctorId == null) return null;
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));
    }
}