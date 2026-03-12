package com.fintechengine.modules.ledger.dto;

import com.fintechengine.modules.ledger.entity.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        String operation,
        BigDecimal amount,
        LocalDateTime createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getTransaction().getId(),
                entry.getAccount().getId(),
                entry.getOperation().name(),
                entry.getAmount(),
                entry.getCreatedAt()
        );
    }
}
