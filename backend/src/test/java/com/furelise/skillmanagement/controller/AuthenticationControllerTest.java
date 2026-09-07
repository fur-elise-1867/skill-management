package com.furelise.skillmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furelise.skillmanagement.dto.AuthenticationRequest;
import com.furelise.skillmanagement.dto.AuthenticationResponse;
import com.furelise.skillmanagement.dto.LogoutRequest;
import com.furelise.skillmanagement.dto.RefreshTokenRequest;
import com.furelise.skillmanagement.service.AuthenticationService;
import com.furelise.skillmanagement.service.BlacklistService;
import com.furelise.skillmanagement.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private BlacklistService blacklistService;

    @Test
    @DisplayName("POST /api/v1/auth/authenticate should return token, accessToken, and refreshToken")
    void testAuthenticateReturnsBothTokens() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("access-token-123", "refresh-token-456");

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-123"))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should rotate tokens and return 200 OK")
    void testRefreshEndpoint() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthenticationResponse response = new AuthenticationResponse("new-access-token", "new-refresh-token");

        when(refreshTokenService.rotateRefreshToken("valid-refresh-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should blacklist access token and revoke refresh token")
    void testLogoutEndpoint() throws Exception {
        LogoutRequest request = new LogoutRequest("refresh-token-to-revoke");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer access.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công."));

        verify(blacklistService).blacklistToken("access.token.here");
        verify(refreshTokenService).revokeToken("refresh-token-to-revoke");
    }
}
