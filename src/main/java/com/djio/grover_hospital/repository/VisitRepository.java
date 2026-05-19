package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    Optional<Visit> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    List<Visit> findByPatientIdOrderByVisitDateDesc(Long patientId);
}