package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.*;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.AdminService;
import com.furelise.skillmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for role and user management.
 * All endpoints require ROLE_ADMIN authority.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Secured("ROLE_ADMIN")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @GetMapping("/get-users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.roleId()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        User currentAdmin = userService.getAuthenticatedUser();
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.enabled(), currentAdmin));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminService.resetPassword(id));
    }

    @DeleteMapping("/delete-user/{email}")
    public ResponseEntity<Map<String, String>> deleteUserByEmail(@PathVariable("email") String email) {
        User currentAdmin = userService.getAuthenticatedUser();
        adminService.deleteUserByEmail(email, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "Đã xóa người dùng thành công."));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUserById(@PathVariable("id") Long id) {
        User currentAdmin = userService.getAuthenticatedUser();
        adminService.deleteUserById(id, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "Đã xóa người dùng thành công."));
    }
}
