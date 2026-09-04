package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 * Uses Java 21 Record for immutability + Bean Validation.
 */
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,

        String gender,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        String mobile,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
