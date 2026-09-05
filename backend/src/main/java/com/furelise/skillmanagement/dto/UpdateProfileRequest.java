package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên không được để trống")
        String name,
        String gender,
        String mobile
) {}
