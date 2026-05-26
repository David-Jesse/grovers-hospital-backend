package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.ProfileUpdateRequest;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileUpdateRequestRepository extends JpaRepository<ProfileUpdateRequest, Long> {

    List<ProfileUpdateRequest> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /** Admin list with optional status filter (pass null to ignore). */
    @Query("""
            SELECT r FROM ProfileUpdateRequest r
            WHERE (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<ProfileUpdateRequest> findForAdmin(@Param("status") ProfileUpdateStatus status, Pageable pageable);
}