package com.contractpulse.user.dto;

import com.contractpulse.user.model.User;
import com.contractpulse.user.model.UserRole;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO de resposta com dados do usuário.
 */
public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String avatarUrl,
        UserRole role,
        ZonedDateTime createdAt
) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
