package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.AuthenticationRequest;
import com.furelise.skillmanagement.dto.AuthenticationResponse;
import com.furelise.skillmanagement.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
