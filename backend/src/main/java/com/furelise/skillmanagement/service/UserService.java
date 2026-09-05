package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ChangePasswordRequest;
import com.furelise.skillmanagement.dto.UpdateProfileRequest;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for user profile operations.
 */
public interface UserService {

    UserResponse getCurrentUserProfile();

    User getAuthenticatedUser();

    UserResponse updateProfile(User user, UpdateProfileRequest request);

    void changePassword(User user, ChangePasswordRequest request);

    String updateAvatar(User user, MultipartFile file);

    void deleteOwnAccount(User user);
}
