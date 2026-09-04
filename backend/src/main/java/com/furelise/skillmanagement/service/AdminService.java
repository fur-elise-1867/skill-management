package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.UserResponse;

import java.util.List;

/**
 * Service interface for admin operations.
 */
public interface AdminService {

    List<UserResponse> getAllUsers();

    void deleteUserByEmail(String email);
}
