package com.furelise.skillmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furelise.skillmanagement.dto.AuthenticationRequest;
import com.furelise.skillmanagement.dto.RegisterRequest;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuthenticationController endpoints:
 * - POST /api/v1/auth/register (TC-AUTH-REG-001 -> TC-AUTH-REG-008)
 * - POST /api/v1/auth/authenticate (TC-AUTH-LOG-001 -> TC-AUTH-LOG-006)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("TC-AUTH-REG-001: Register successfully with all valid fields")
        void shouldRegisterSuccessfullyWithAllFields() throws Exception {
            var request = new RegisterRequest(
                    "John Doe",
                    "Male",
                    "newuser@example.com",
                    "0912345678",
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token", not(emptyOrNullString())));

            var savedUser = userRepository.findByEmail("newuser@example.com");
            assertThat(savedUser).isPresent();
            assertThat(savedUser.get().getName()).isEqualTo("John Doe");
            assertThat(savedUser.get().getRole()).isEqualTo(Role.USER);
            assertThat(savedUser.get().getGender()).isEqualTo("Male");
            assertThat(savedUser.get().getMobile()).isEqualTo("0912345678");
            assertThat(passwordEncoder.matches("password123", savedUser.get().getPassword())).isTrue();
        }

        @Test
        @DisplayName("TC-AUTH-REG-002: Register successfully with minimal required fields (gender & mobile null)")
        void shouldRegisterSuccessfullyWithMinimalFields() throws Exception {
            var request = new RegisterRequest(
                    "Minimal User",
                    null,
                    "minimal@example.com",
                    null,
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token", not(emptyOrNullString())));

            var savedUser = userRepository.findByEmail("minimal@example.com");
            assertThat(savedUser).isPresent();
            assertThat(savedUser.get().getGender()).isNull();
            assertThat(savedUser.get().getMobile()).isNull();
        }

        @Test
        @DisplayName("TC-AUTH-REG-003: Register fails when name is blank (Validation 400)")
        void shouldFailWhenNameIsBlank() throws Exception {
            var request = new RegisterRequest(
                    "",
                    "Male",
                    "valid@example.com",
                    null,
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.name", is("Name is required")));
        }

        @Test
        @DisplayName("TC-AUTH-REG-004: Register fails when email is blank (Validation 400)")
        void shouldFailWhenEmailIsBlank() throws Exception {
            var request = new RegisterRequest(
                    "Name",
                    "Male",
                    "",
                    null,
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.email", notNullValue()));
        }

        @Test
        @DisplayName("TC-AUTH-REG-005: Register fails when email format is invalid (Validation 400)")
        void shouldFailWhenEmailFormatIsInvalid() throws Exception {
            var request = new RegisterRequest(
                    "Name",
                    "Male",
                    "invalid-email-format",
                    null,
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.email", is("Invalid email format")));
        }

        @Test
        @DisplayName("TC-AUTH-REG-006: Register fails when password is blank (Validation 400)")
        void shouldFailWhenPasswordIsBlank() throws Exception {
            var request = new RegisterRequest(
                    "Name",
                    "Male",
                    "valid@example.com",
                    null,
                    ""
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.password", notNullValue()));
        }

        @Test
        @DisplayName("TC-AUTH-REG-007: Register fails when password is less than 8 characters (Validation 400)")
        void shouldFailWhenPasswordIsLessThan8Characters() throws Exception {
            var request = new RegisterRequest(
                    "Name",
                    "Male",
                    "valid@example.com",
                    null,
                    "1234567"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.password", is("Password must be at least 8 characters")));
        }

        @Test
        @DisplayName("TC-AUTH-REG-008: Register fails when email already exists (Duplicate 400)")
        void shouldFailWhenEmailAlreadyExists() throws Exception {
            var existingUser = User.builder()
                    .name("Existing User")
                    .email("existing@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(existingUser);

            var request = new RegisterRequest(
                    "Duplicate User",
                    "Other",
                    "existing@example.com",
                    "0900000000",
                    "password123"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("Email đã được đăng ký: existing@example.com")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/authenticate")
    class AuthenticateTests {

        @BeforeEach
        void setupUser() {
            var user = User.builder()
                    .name("Auth Test User")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }

        @Test
        @DisplayName("TC-AUTH-LOG-001: Authenticate successfully with correct credentials")
        void shouldAuthenticateSuccessfully() throws Exception {
            var request = new AuthenticationRequest("user@example.com", "password123");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", not(emptyOrNullString())));
        }

        @Test
        @DisplayName("TC-AUTH-LOG-002: Authenticate fails with incorrect password (401)")
        void shouldFailWithIncorrectPassword() throws Exception {
            var request = new AuthenticationRequest("user@example.com", "wrongpassword");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status", is(401)))
                    .andExpect(jsonPath("$.message", is("Email hoặc mật khẩu không chính xác.")));
        }

        @Test
        @DisplayName("TC-AUTH-LOG-003: Authenticate fails with non-existent email (401)")
        void shouldFailWithNonExistentEmail() throws Exception {
            var request = new AuthenticationRequest("nonexistent@example.com", "password123");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status", is(401)))
                    .andExpect(jsonPath("$.message", is("Email hoặc mật khẩu không chính xác.")));
        }

        @Test
        @DisplayName("TC-AUTH-LOG-004: Authenticate fails when email is blank (Validation 400)")
        void shouldFailWhenEmailIsBlank() throws Exception {
            var request = new AuthenticationRequest("", "password123");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.email", notNullValue()));
        }

        @Test
        @DisplayName("TC-AUTH-LOG-005: Authenticate fails when password is blank (Validation 400)")
        void shouldFailWhenPasswordIsBlank() throws Exception {
            var request = new AuthenticationRequest("user@example.com", "");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.password", notNullValue()));
        }

        @Test
        @DisplayName("TC-AUTH-LOG-006: Authenticate fails when email is invalid format (Validation 400)")
        void shouldFailWhenEmailIsInvalidFormat() throws Exception {
            var request = new AuthenticationRequest("not-an-email", "password123");

            mockMvc.perform(post("/api/v1/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.details.email", is("Invalid email format")));
        }
    }
}
