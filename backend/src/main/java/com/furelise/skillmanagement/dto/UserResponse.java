package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.Role;

import java.time.Instant;

/**
 * DTO for returning user data without sensitive fields (password).
 * Fixes the password hash leak vulnerability.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String gender,
        String mobile,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Factory method to create UserResponse from User entity.
     */
    public static UserResponse from(com.furelise.skillmanagement.model.User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getMobile(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
