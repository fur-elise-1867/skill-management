package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ChangePasswordRequest;
import com.furelise.skillmanagement.dto.UpdateProfileRequest;
import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, fileStorageService);
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email("user@test.com")
                .role(Role.builder().name("USER").build())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest("New Name", "Male", "0987654321");
        UserResponse response = userService.updateProfile(user, request);

        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getGender()).isEqualTo("Male");
        assertThat(user.getMobile()).isEqualTo("0987654321");
        verify(userRepository).save(user);
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatches() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encodedOldPass")
                .build();

        when(passwordEncoder.matches("oldPass123", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");
        userService.changePassword(user, request);

        assertThat(user.getPassword()).isEqualTo("encodedNewPass");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordIncorrect() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encodedOldPass")
                .build();

        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass123");
        assertThatThrownBy(() -> userService.changePassword(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mật khẩu hiện tại không chính xác");
    }

    @Test
    void shouldUpdateAvatarSuccessfully() {
        User user = User.builder()
                .id(1L)
                .avatarUrl("/uploads/avatars/old.png")
                .build();

        MockMultipartFile file = new MockMultipartFile("avatar", "new.png", "image/png", "content".getBytes());
        when(fileStorageService.storeAvatar(1L, file, "/uploads/avatars/old.png"))
                .thenReturn("/uploads/avatars/avatar_1_123.png");

        String newUrl = userService.updateAvatar(user, file);

        assertThat(newUrl).isEqualTo("/uploads/avatars/avatar_1_123.png");
        assertThat(user.getAvatarUrl()).isEqualTo("/uploads/avatars/avatar_1_123.png");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectSelfDeleteIfAdmin() {
        User admin = User.builder()
                .id(1L)
                .email("admin@test.com")
                .role(Role.builder().name("ADMIN").build())
                .build();

        assertThatThrownBy(() -> userService.deleteOwnAccount(admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quản trị viên (Admin) không được phép tự xóa");
    }

    @Test
    void shouldAllowSelfDeleteIfUser() {
        User user = User.builder()
                .id(2L)
                .email("user@test.com")
                .role(Role.builder().name("USER").build())
                .avatarUrl("/uploads/avatars/test.png")
                .build();

        userService.deleteOwnAccount(user);

        verify(fileStorageService).deleteAvatar("/uploads/avatars/test.png");
        verify(userRepository).deleteById(2L);
    }
}
