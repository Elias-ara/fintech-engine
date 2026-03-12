package com.fintechengine.modules.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawalRequest(
        @NotNull UUID accountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String idempotencyKey
) {}
