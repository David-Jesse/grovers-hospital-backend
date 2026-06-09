package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.AdminUpdatePatientRequest;
import com.djio.grover_hospital.model.dto.response.AdminPatientResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPatientService {

    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public PageResponse<AdminPatientResponse> search(String search, Pageable pageable) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<Patient> page = (normalized == null)
                ? patientRepository.findAll(pageable)
                : patientRepository.searchByText(normalized, pageable);
        return PageResponse.from(page, AdminPatientResponse::from);
    }

    public AdminPatientResponse getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        return AdminPatientResponse.from(patient);
    }

    @Transactional
    public AdminPatientResponse update(Long id, AdminUpdatePatientRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        // If email is changing, make sure no other patient already has it.
        // Email comparison is case-insensitive to match how the DB unique constraint usually behaves;
        // if your column is citext or has LOWER(email) indexed, this matches that.
        if (!patient.getEmail().equalsIgnoreCase(request.getEmail())) {
            patientRepository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new BadRequestException("Email already in use by another patient");
                    });
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setWhatsappNumber(request.getWhatsappNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());

        Patient saved = patientRepository.save(patient);

        auditService.log(
                SecurityUtils.getCurrentUserId(),
                "ADMIN",
                "UPDATE_PATIENT_BASIC_INFO",
                "Patient",
                saved.getId(),
                httpRequest
        );

        return AdminPatientResponse.from(saved);
    }
}