package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Trạng thái enabled không được để trống")
        Boolean enabled
) {}
