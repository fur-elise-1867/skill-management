package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;

import java.time.Instant;

/**
 * DTO for returning user data without sensitive fields (password).
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String gender,
        String mobile,
        Role role,
        boolean enabled,
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Factory method to create UserResponse from User entity.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getMobile(),
                user.getRole(),
                user.isEnabled(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
