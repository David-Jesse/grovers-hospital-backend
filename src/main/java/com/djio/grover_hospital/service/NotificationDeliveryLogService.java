package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.dto.response.NotificationDeliveryLogResponse;
import com.djio.grover_hospital.model.entity.NotificationDeliveryLog;
import com.djio.grover_hospital.model.enums.DeliveryChannel;
import com.djio.grover_hospital.model.enums.DeliveryStatus;
import com.djio.grover_hospital.model.enums.NotificationEvent;
import com.djio.grover_hospital.notification.core.SendResult;
import com.djio.grover_hospital.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Persists delivery attempts for support investigations and admin visibility.
 *
 * <p>The typical call shape from DefaultNotificationService is:</p>
 * <pre>
 *     SendResult result = emailSender.send(emailMessage);
 *     deliveryLogService.record(patientId, NotificationEvent.RESULT_READY,
 *             "RESULT", resultId, DeliveryChannel.EMAIL, recipient, result);
 * </pre>
 *
 * <p>Runs in REQUIRES_NEW so a log-write failure never rolls back the caller's business
 * transaction (and vice versa — a business rollback never deletes the log row).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryLogService {

    private final NotificationDeliveryLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDeliveryLog record(Long patientId,
                                          NotificationEvent event,
                                          String referenceType,
                                          Long referenceId,
                                          DeliveryChannel channel,
                                          String recipient,
                                          SendResult result) {
        DeliveryStatus status = result.isSuccess() ? DeliveryStatus.SENT : DeliveryStatus.FAILED;

        NotificationDeliveryLog logRow = NotificationDeliveryLog.builder()
                .patientId(patientId)
                .eventName(event.name())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .channel(channel)
                .status(status)
                .providerMessageId(result.getProviderMessageId())
                .recipient(recipient)
                .errorMessage(result.getErrorMessage())
                .sentAt(result.isSuccess() ? OffsetDateTime.now() : null)
                .build();

        return repository.save(logRow);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDeliveryLogResponse> list(Long patientId, DeliveryChannel channel, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<NotificationDeliveryLog> results;

        if (patientId != null && channel != null) {
            results = repository.findByPatientIdAndChannelOrderByCreatedAtDesc(patientId, channel, pageable);
        } else if (patientId != null) {
            results = repository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        } else {
            results = repository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return results.map(NotificationDeliveryLogResponse::from);
    }
}