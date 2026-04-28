package com.djio.grover_hospital.config;


import com.djio.grover_hospital.model.entity.Admin;
import com.djio.grover_hospital.model.enums.Role;
import com.djio.grover_hospital.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedDefaultAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultEmail = "admin@grovershospital.com";

            if (!adminRepository.existsByEmail(defaultEmail)) {
                Admin admin = Admin.builder()
                        .fullName("System Adminstrator")
                        .email(defaultEmail)
                        .passwordHash(passwordEncoder.encode("ChangeMe@2024"))
                        .role(Role.ADMIN)
                        .build();
                adminRepository.save(admin);
                logger.info("Default admin created: {} (password: ChangeMe@2024), CHANGE THIS IN PRODUCTION!", defaultEmail);
            } else {
                logger.info("Default admin already exists, skipping seed");
            }
        };
    }
}
