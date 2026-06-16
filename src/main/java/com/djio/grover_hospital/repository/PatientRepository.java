package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);

    /** All patients, paginated. */
    Page<Patient> findAllByOrderByIdDesc(Pageable pageable);
// or keep using JpaRepository.findAll(pageable) — same idea

    /** Patients matching a free-text search across name, email, phone. */
    @Query("""
        SELECT p FROM Patient p
        WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.email)     LIKE LOWER(CONCAT('%', :search, '%'))
           OR p.phone            LIKE CONCAT('%', :search, '%')
        """)
    Page<Patient> searchByText(@Param("search") String search, Pageable pageable);

}