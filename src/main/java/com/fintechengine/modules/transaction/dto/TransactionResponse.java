package com.fintechengine.modules.transaction.dto;

import com.fintechengine.modules.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String idempotencyKey,
        String operationType,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getIdempotencyKey(),
                t.getOperationType().name(),
                t.getAmount(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}
