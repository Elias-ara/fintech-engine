package com.fintechengine.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Size(min = 11, max = 14) String document,
        @NotBlank @Email String email
) {}
