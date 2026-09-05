package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.CreateUserRequest;
import com.furelise.skillmanagement.dto.ResetPasswordResponse;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.RoleRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FileStorageService fileStorageService;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository, roleRepository, passwordEncoder, fileStorageService);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        Role role = Role.builder().id(2L).name("EDITOR").build();
        when(userRepository.findByEmail("editor@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        User savedUser = User.builder()
                .id(5L)
                .name("Editor Name")
                .email("editor@test.com")
                .role(role)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CreateUserRequest request = new CreateUserRequest(
                "Editor Name", "editor@test.com", "password123", 2L, "Male", "0123456789", true
        );

        UserResponse response = adminService.createUser(request);
        assertThat(response.email()).isEqualTo("editor@test.com");
        assertThat(response.role().getName()).isEqualTo("EDITOR");
    }

    @Test
    void shouldRejectChangingRoleOfAdmin() {
        User targetAdmin = User.builder()
                .id(10L)
                .email("super@test.com")
                .role(Role.builder().id(1L).name("ADMIN").build())
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(targetAdmin));

        assertThatThrownBy(() -> adminService.updateUserRole(10L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể thay đổi hoặc hạ cấp vai trò của tài khoản Quản trị viên");
    }

    @Test
    void shouldAllowChangingRoleOfNonAdmin() {
        Role editorRole = Role.builder().id(2L).name("EDITOR").build();
        Role userRole = Role.builder().id(3L).name("USER").build();

        User targetUser = User.builder()
                .id(10L)
                .email("editor@test.com")
                .role(editorRole)
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(targetUser));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        UserResponse response = adminService.updateUserRole(10L, 3L);
        assertThat(targetUser.getRole().getName()).isEqualTo("USER");
    }

    @Test
    void shouldRejectAdminSelfDeactivation() {
        User currentAdmin = User.builder().id(1L).email("admin@test.com").build();
        User targetUser = User.builder().id(1L).email("admin@test.com").enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        assertThatThrownBy(() -> adminService.updateUserStatus(1L, false, currentAdmin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bạn không thể tự khóa tài khoản của chính mình");
    }

    @Test
    void shouldAllowDeactivatingOtherUser() {
        User currentAdmin = User.builder().id(1L).email("admin@test.com").build();
        User targetUser = User.builder().id(2L).email("user@test.com").enabled(true).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        UserResponse response = adminService.updateUserStatus(2L, false, currentAdmin);
        assertThat(targetUser.isEnabled()).isFalse();
    }

    @Test
    void shouldResetPasswordAndReturnNewRawPassword() {
        User targetUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .password("oldEncoded")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        ResetPasswordResponse response = adminService.resetPassword(2L);

        assertThat(response.newPassword()).hasSizeGreaterThanOrEqualTo(10);
        verify(userRepository).save(targetUser);
    }
}
