package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Họ tên không được để trống")
        String name,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có tối thiểu 8 ký tự")
        String password,

        @NotNull(message = "Vai trò không được để trống")
        Long roleId,

        String gender,
        String mobile,
        Boolean enabled
) {}
