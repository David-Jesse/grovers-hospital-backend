package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.ChangePasswordRequest;
import com.djio.grover_hospital.model.dto.response.PatientProfileResponse;
import com.djio.grover_hospital.model.dto.request.UpdateProfileRequest;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PatientProfileService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientProfileResponse getCurrentProfile() {
        Patient patient = loadCurrentPatient();
        return PatientProfileResponse.from(patient);
    }

    @Transactional
    public PatientProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
        Patient patient = loadCurrentPatient();

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());

        Patient updated = patientRepository.save(patient);
        log.info("Profile updated for patient: {}", updated.getId());

        return PatientProfileResponse.from(updated);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Patient patient = loadCurrentPatient();

        // Verify current password before allowing change
        if (!passwordEncoder.matches(request.getCurrentPassword(), patient.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Reject if new password is identical to current
        if (passwordEncoder.matches(request.getNewPassword(), patient.getPasswordHash())) {
            throw new BadRequestException("New password must be different from your current password");
        }

        patient.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        patientRepository.save(patient);

        log.info("Password changed for patient {}", patient.getId());
    }

    private Patient loadCurrentPatient() {
        Long patientId = SecurityUtils.getCurrentUserId();
        if (patientId == null) {
            throw new UnauthorizedException("No active session");
        }
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));
    }


}
