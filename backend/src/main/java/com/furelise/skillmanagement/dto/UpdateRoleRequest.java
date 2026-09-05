package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "RoleId không được để trống")
        Long roleId
) {}
