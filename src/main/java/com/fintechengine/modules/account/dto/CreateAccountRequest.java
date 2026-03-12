package com.fintechengine.modules.account.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(@NotNull UUID userId) {}
