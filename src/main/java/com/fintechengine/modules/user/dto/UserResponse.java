package com.fintechengine.modules.user.dto;

import com.fintechengine.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String name, String document, String email, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getDocument(), user.getEmail(), user.getCreatedAt());
    }
}
