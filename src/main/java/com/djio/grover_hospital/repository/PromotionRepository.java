package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Returns promotions that are currently active and within their date window
     * (or that have no date window set)
     */
    @Query("""
            SELECT p FROM Promotion p
            WHERE p.isActive = true
            AND (p.startsAt IS NULL OR p.startsAt <= :now)
            AND (p.endsAt IS NULL OR p.endsAt >= :now)
            ORDER BY p.displayOrder ASC
            """)
    List<Promotion> findCurrentlyActive(@Param("now")OffsetDateTime now);

    List<Promotion> findAllByOrderByDisplayOrderAsc();
}