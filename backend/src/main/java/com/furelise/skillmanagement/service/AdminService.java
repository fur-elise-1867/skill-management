package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.CreateUserRequest;
import com.furelise.skillmanagement.dto.ResetPasswordResponse;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;

import java.util.List;

/**
 * Service interface for admin operations.
 */
public interface AdminService {

    List<Role> getAllRoles();

    List<UserResponse> getAllUsers();

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUserRole(Long userId, Long roleId);

    UserResponse updateUserStatus(Long userId, boolean enabled, User currentAdmin);

    ResetPasswordResponse resetPassword(Long userId);

    void deleteUserById(Long userId, User currentAdmin);

    void deleteUserByEmail(String email, User currentAdmin);
}
