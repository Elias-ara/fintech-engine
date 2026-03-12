package com.fintechengine.modules.account.dto;

import com.fintechengine.modules.account.entity.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String status,
        BigDecimal balance,
        LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUser().getId(),
                account.getStatus().name(),
                account.getCachedBalance(),
                account.getCreatedAt()
        );
    }
}
