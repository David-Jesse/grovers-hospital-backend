package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.model.dto.request.LoginRequest;
import com.djio.grover_hospital.model.dto.request.RegisterRequest;
import com.djio.grover_hospital.model.dto.response.AuthResponse;
import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.Role;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (patientRepository.existsByEmail(email) || adminRepository.existsByEmail(email)) {
            throw new BadRequestException(("An account with this email already exists"));
        }

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        patient = patientRepository.save(patient);

        return generateAuthResponse(
                patient.getId(),
                patient.getEmail(),
                Role.PATIENT,
                patient.getFirstName() + " " + patient.getLastName()
        );
    }

    public AuthResponse loginPatient(LoginRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!patient.getIsActive()) {
            throw new BadRequestException("This account has been deactivated. Please contact the hospital");
        }

        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return generateAuthResponse(
                patient.getId(),
                patient.getEmail(),
                Role.PATIENT,
                patient.getFirstName() + " " + patient.getLastName()
        );
    }

    public AuthResponse loginAdmin(LoginRequest request) {
        Admin admin = adminRepository.findbyEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return generateAuthResponse(admin.getId(), admin.getEmail(), Role.ADMIN, admin.getFullName());
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException(("Invalid or expired refresh token"));
        }

        if (!"refresh".equals(tokenProvider.getTokenType(refreshToken))) {
            throw new BadRequestException("Invalid token type. Expected a refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        String email = tokenProvider.getEmailFromToken(refreshToken);
        Role role = tokenProvider.getRoleFromToken(refreshToken);

        if (role == Role.PATIENT) {
            Patient patient = patientRepository.findById(userId)
                    .orElseThrow(() -> new BadRequestException("User no longer exists"));
            if (!patient.getIsActive()) {
                throw new BadRequestException("Account has been deactivated");
            }
            return generateAuthResponse(userId, email, role,
                patient.getFirstName() + " " + patient.getLastName()
            );
        } else {
            Admin admin = adminRepository.findById(userId)
                    .orElseThrow(() -> new BadRequestException("User no longer exists"));
            return generateAuthResponse(userId, email, role, admin.getFullName());
        }
    }

    private AuthResponse generateAuthResponse(Long userId, String email, Role role, String fullName) {
        String accessToken = tokenProvider.generateAccessToken(userId, email, role);
        String refreshToken = tokenProvider.generateRefreshToken(userId, email, role);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getAccessExpirationMs(),
                role,
                fullName,
                email
        );
    }
}
