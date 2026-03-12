package com.fintechengine.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Size(min = 11, max = 14) String document,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {}
