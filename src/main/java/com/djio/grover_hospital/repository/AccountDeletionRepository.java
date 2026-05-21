package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.AccountDeletionRequest;
import com.djio.grover_hospital.model.enums.AccountDeletionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountDeletionRepository extends JpaRepository<AccountDeletionRequest, Long> {

    Optional<AccountDeletionRequest> findByPatientIdAndStatus(Long patientId, AccountDeletionStatus status);

    /**
     * Find pending deletions whose grace period has expired - for daily cron
     */
    List<AccountDeletionRequest> findByStatusAndScheduledForBefore(AccountDeletionStatus status, OffsetDateTime cutoff);
}
