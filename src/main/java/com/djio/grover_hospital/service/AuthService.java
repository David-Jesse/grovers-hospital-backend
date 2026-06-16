package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.model.dto.request.ForgotPasswordRequest;
import com.djio.grover_hospital.model.dto.request.LoginRequest;
import com.djio.grover_hospital.model.dto.request.RegisterRequest;
import com.djio.grover_hospital.model.dto.request.ResetPasswordRequest;
import com.djio.grover_hospital.model.dto.response.AuthResponse;
import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.PasswordResetToken;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.Role;
import com.djio.grover_hospital.notification.NotificationService;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.PasswordResetTokenRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import com.djio.grover_hospital.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationService notificationService;
// PatientRepository and PasswordEncoder you already have

    private static final int TOKEN_BYTES = 32;             // 256 bits
    private static final long TOKEN_TTL_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

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

    /**
     * Initiates a password reset. Always returns success — we never leak whether an email
     * is registered. If the email does belong to a patient, a token is generated, the hash
     * is stored, and the plaintext token is emailed (async).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String requestingIp) {
        Optional<Patient> patientOpt = patientRepository.findByEmail(request.getEmail());
        if (patientOpt.isEmpty()) {
            // Silent no-op. The endpoint still returns "success" to the caller.
            return;
        }

        Patient patient = patientOpt.get();
        String plaintextToken = generateToken();
        String hash = sha256Hex(plaintextToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .patientId(patient.getId())
                .tokenHash(hash)
                .expiresAt(OffsetDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .requestingIp(requestingIp)
                .build();
        passwordResetTokenRepository.save(token);

        // Existing notification method — sends email with the plaintext token + 30-min expiry copy
        notificationService.notifyPasswordResetLink(patient, plaintextToken);
    }

    /**
     * Validates the token and updates the patient's password. Marks the token as used so
     * it can't be replayed.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String hash = sha256Hex(request.getToken());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (!token.isUsable(OffsetDateTime.now())) {
            throw new BadRequestException("Invalid or expired token");
        }

        Patient patient = patientRepository.findById(token.getPatientId())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        patient.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        patientRepository.save(patient);

        token.setUsedAt(OffsetDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    /** 32-byte random token, base64url-encoded without padding (~43 chars). */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 hex digest. */
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
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
        Admin admin = adminRepository.findByEmail(request.getEmail().toLowerCase().trim())
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

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getAccessExpirationMs())
                .role(role)
                .fullName(fullName)
                .email(email)
                .build();
    }
}