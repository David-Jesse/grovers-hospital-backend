package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Page<Result> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Page<Result> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
