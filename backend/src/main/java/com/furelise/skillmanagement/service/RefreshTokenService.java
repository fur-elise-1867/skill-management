package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.AuthenticationResponse;
import com.furelise.skillmanagement.model.RefreshToken;
import com.furelise.skillmanagement.model.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    AuthenticationResponse rotateRefreshToken(String requestRefreshToken);

    void revokeToken(String token);

    void revokeAllUserTokens(Long userId);
}
