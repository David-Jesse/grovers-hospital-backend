package com.djio.grover_hospital.security;


import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.entity.Patient;
import com.djio.grover_hospital.model.enums.Role;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;

    /**
     * Spring security calls this during authentication.
     * Checks patient table first, then Admin.
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) {
            Patient p = patient.get();
            return new UserPrincipal(
                    p.getId(), p.getEmail(), p.getPasswordHash(), Role.PATIENT, p.getIsActive()
            );
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new UserPrincipal(
                admin.getId(), admin.getEmail(), admin.getPasswordHash(), Role.ADMIN, true
        );
    }

    /**
     * Called by the JWT filter after token validation to rebuild the UserPrincipal
     * Uses ID + role from the token to look up the correct table directly
     */

    public UserPrincipal loadUserByIdAndRole(Long id, Role role) {
        if (role == Role.PATIENT) {
            Patient p = patientRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Patient not found with id: " + id));
            return new UserPrincipal(
                    p.getId(), p.getEmail(), p.getPasswordHash(), Role.PATIENT, p.getIsActive()
            );
        } else {
            Admin a = adminRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found with id: " + id));
            return new UserPrincipal(
                    a.getId(), a.getEmail(), a.getPasswordHash(), Role.ADMIN, true
            );
        }
    }
}