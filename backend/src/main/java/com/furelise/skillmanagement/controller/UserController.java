package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.ChangePasswordRequest;
import com.furelise.skillmanagement.dto.UpdateProfileRequest;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Endpoints for authenticated users to manage their profile, password, avatar and account.
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = userService.getAuthenticatedUser();
        userService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công."));
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        User user = userService.getAuthenticatedUser();
        String avatarUrl = userService.updateAvatar(user, file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        User user = userService.getAuthenticatedUser();
        userService.deleteOwnAccount(user);
        return ResponseEntity.ok(Map.of("message", "Đã xóa tài khoản thành công."));
    }
}
