package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for user management.
 * Requires ROLE_ADMIN authority.
 * Returns UserResponse DTOs (no password hash exposure).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/get-users")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/delete-user/{email}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<String> deleteUser(@PathVariable("email") String email) {
        adminService.deleteUserByEmail(email);
        return ResponseEntity.ok("User deleted successfully.");
    }
}
