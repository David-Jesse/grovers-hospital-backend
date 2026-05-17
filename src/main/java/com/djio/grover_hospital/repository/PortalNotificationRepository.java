package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.PortalNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface PortalNotificationRepository extends JpaRepository<PortalNotification, Long> {

    Page<PortalNotification> findByUserIdAndUserTypeOrderByCreatedAtDesc(
            Long userId, String userType, Pageable pageable
    );

    Page<PortalNotification> findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(
            Long userId, String userType, Boolean isRead, Pageable pageable
    );

    long countByUserIdAndUserTypeAndIsRead(Long userId, String userType, Boolean isRead);

    @Modifying
    @Query("UPDATE PortalNotification n SET n.isRead = true " +
            "WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false"
    )
    int markAllAsRead(@Param("userId") Long userId, @Param("userType") String userType);

    @Modifying
    @Query("DELETE FROM PortalNotification n WHERE n.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") OffsetDateTime cutoff);
}
