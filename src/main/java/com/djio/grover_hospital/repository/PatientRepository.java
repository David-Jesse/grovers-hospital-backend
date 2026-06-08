package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Patient;
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

    @Query("""
        SELECT p FROM Patient p
        WHERE :search IS NULL
           OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.email)     LIKE LOWER(CONCAT('%', :search, '%'))
           OR p.phone            LIKE CONCAT('%', :search, '%')
        """)
    Page<Patient> searchAll(@Param("search") String search, Pageable pageable);
}