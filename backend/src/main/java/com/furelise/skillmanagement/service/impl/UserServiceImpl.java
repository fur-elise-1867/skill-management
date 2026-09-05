package com.furelise.skillmanagement.service.impl;

import com.furelise.skillmanagement.dto.ChangePasswordRequest;
import com.furelise.skillmanagement.dto.UpdateProfileRequest;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.FileStorageService;
import com.furelise.skillmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of UserService for profile, password, avatar and account deletion.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    public User getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new UsernameNotFoundException("No authenticated user found.");
        }
        return userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUser.getEmail()));
    }

    @Override
    public UserResponse getCurrentUserProfile() {
        return UserResponse.from(getAuthenticatedUser());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setName(request.name());
        user.setGender(request.gender());
        user.setMobile(request.mobile());
        User updated = userRepository.save(user);
        return UserResponse.from(updated);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String updateAvatar(User user, MultipartFile file) {
        String newAvatarUrl = fileStorageService.storeAvatar(user.getId(), file, user.getAvatarUrl());
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);
        return newAvatarUrl;
    }

    @Override
    @Transactional
    public void deleteOwnAccount(User user) {
        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Tài khoản Quản trị viên (Admin) không được phép tự xóa tài khoản.");
        }
        if (user.getAvatarUrl() != null) {
            fileStorageService.deleteAvatar(user.getAvatarUrl());
        }
        userRepository.deleteById(user.getId());
    }
}
