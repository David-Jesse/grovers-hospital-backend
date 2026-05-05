package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findByIsReadOrderByCreatedAtDesc(Boolean isRead, Pageable pageable);

    Page<Feedback> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    long countByIsRead(Boolean isRead);
}
