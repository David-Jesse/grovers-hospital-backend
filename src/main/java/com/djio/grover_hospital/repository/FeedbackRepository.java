package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.Feedback;
import com.djio.grover_hospital.model.enums.FeedbackSource;
import com.djio.grover_hospital.model.enums.FeedbackStatus;
import com.djio.grover_hospital.model.enums.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findByIsReadOrderByCreatedAtDesc(Boolean isRead, Pageable pageable);

    Page<Feedback> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    List<Feedback> findByPatientIdAndSourceOrderByCreatedAtDesc(Long patientId, FeedbackSource source);

    long countByIsRead(Boolean isRead);

    /**
     * Admin list with optional filters. Pass null for any to skip that filter.
     */
    @Query("""
            SELECT f FROM Feedback f
            WHERE (:source IS NULL OR f.source = :source)
              AND (:status IS NULL OR f.status = :status)
              AND (:type IS NULL OR f.type = :type)
              AND (:isRead IS NULL OR f.isRead = :isRead)
            ORDER BY f.createdAt DESC
            """)
    Page<Feedback> findForAdminWithFilters(@Param("source") FeedbackSource source,
                                           @Param("status") FeedbackStatus status,
                                           @Param("type") FeedbackType type,
                                           @Param("isRead") Boolean isRead,
                                           Pageable pageable);
}
