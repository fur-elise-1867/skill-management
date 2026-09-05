package com.furelise.skillmanagement.service.impl;

import com.furelise.skillmanagement.dto.CreateUserRequest;
import com.furelise.skillmanagement.dto.ResetPasswordResponse;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.RoleRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.AdminService;
import com.furelise.skillmanagement.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin service implementation for user & role management.
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã được đăng ký: " + request.email());
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không tồn tại với ID: " + request.roleId()));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .gender(request.gender())
                .mobile(request.mobile())
                .enabled(request.enabled() != null ? request.enabled() : true)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, Long roleId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (targetUser.getRole() != null && "ADMIN".equalsIgnoreCase(targetUser.getRole().getName())) {
            throw new IllegalArgumentException("Không thể thay đổi hoặc hạ cấp vai trò của tài khoản Quản trị viên (Admin).");
        }

        Role newRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không tồn tại với ID: " + roleId));

        targetUser.setRole(newRole);
        User updated = userRepository.save(targetUser);
        return UserResponse.from(updated);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled, User currentAdmin) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (currentAdmin != null && targetUser.getId().equals(currentAdmin.getId()) && !enabled) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình.");
        }

        targetUser.setEnabled(enabled);
        User updated = userRepository.save(targetUser);
        return UserResponse.from(updated);
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        String rawPassword = generateRandomPassword();
        targetUser.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(targetUser);

        return new ResetPasswordResponse(rawPassword);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId, User currentAdmin) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (currentAdmin != null && targetUser.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Bạn không thể tự xóa tài khoản của chính mình.");
        }

        if (targetUser.getAvatarUrl() != null) {
            fileStorageService.deleteAvatar(targetUser.getAvatarUrl());
        }

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email, User currentAdmin) {
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        if (currentAdmin != null && targetUser.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Bạn không thể tự xóa tài khoản của chính mình.");
        }

        if (targetUser.getAvatarUrl() != null) {
            fileStorageService.deleteAvatar(targetUser.getAvatarUrl());
        }

        userRepository.deleteById(targetUser.getId());
    }

    private String generateRandomPassword() {
        List<Character> chars = new ArrayList<>();
        chars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        for (int i = 4; i < 10; i++) {
            chars.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(chars, RANDOM);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
}
