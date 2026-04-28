package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    Page<AccessLog> findByUserIdAndUserType(Long userId, String userType, Pageable pageable);

    Page<AccessLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);

    Page<AccessLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}