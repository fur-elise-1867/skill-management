package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.*;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.AuthenticationService;
import com.furelise.skillmanagement.service.BlacklistService;
import com.furelise.skillmanagement.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints for registration, login, refresh token, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(refreshTokenService.rotateRefreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            blacklistService.blacklistToken(accessToken);
        }

        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            refreshTokenService.revokeToken(request.refreshToken());
        }

        return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công."));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal User currentUser
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            blacklistService.blacklistToken(accessToken);
        }

        if (currentUser != null) {
            refreshTokenService.revokeAllUserTokens(currentUser.getId());
        }

        return ResponseEntity.ok(new MessageResponse("Đã đăng xuất khỏi tất cả các thiết bị."));
    }
}
