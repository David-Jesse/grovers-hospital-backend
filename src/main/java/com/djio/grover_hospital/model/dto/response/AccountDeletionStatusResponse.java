package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.AccountDeletionRequest;
import com.djio.grover_hospital.model.enums.AccountDeletionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionStatusResponse {

    private Long id;
    private AccountDeletionStatus status;
    private OffsetDateTime scheduledFor;
    private OffsetDateTime createdAt;
    private String reason;

    public static AccountDeletionStatusResponse from(AccountDeletionRequest r) {
        return AccountDeletionStatusResponse.builder()
                .id(r.getId())
                .status(r.getStatus())
                .scheduledFor(r.getScheduledFor())
                .createdAt(r.getCreatedAt())
                .reason(r.getReason())
                .build();
    }
}