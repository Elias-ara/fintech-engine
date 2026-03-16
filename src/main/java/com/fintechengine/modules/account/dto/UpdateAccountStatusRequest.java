package com.fintechengine.modules.account.dto;

import com.fintechengine.modules.account.entity.Account;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(
        @NotNull Account.Status status
) {
}
