package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.NotificationDeliveryLog;
import com.djio.grover_hospital.model.enums.DeliveryChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    Page<NotificationDeliveryLog> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Page<NotificationDeliveryLog> findByPatientIdAndChannelOrderByCreatedAtDesc(
            Long patientId, DeliveryChannel channel, Pageable pageable);

    Page<NotificationDeliveryLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<NotificationDeliveryLog> findByProviderMessageId(String providerMessageId);
}