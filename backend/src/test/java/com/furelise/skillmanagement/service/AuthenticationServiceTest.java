package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.AuthenticationRequest;
import com.furelise.skillmanagement.dto.AuthenticationResponse;
import com.furelise.skillmanagement.dto.RegisterRequest;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.RoleRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(
                userRepository, roleRepository, passwordEncoder, jwtService, authenticationManager
        );
    }

    @Test
    void shouldRegisterWithDefaultRoleUserAndEnabledTrue() {
        Role userRole = Role.builder().id(3L).name("USER").build();
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        RegisterRequest request = new RegisterRequest("New User", "Male", "new@test.com", "0123456789", "password123");
        AuthenticationResponse response = authenticationService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRole().getName()).isEqualTo("USER");
    }

    @Test
    void shouldPropagateDisabledExceptionWhenAccountIsDisabled() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        AuthenticationRequest request = new AuthenticationRequest("disabled@test.com", "password123");

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(DisabledException.class);
    }
}
