package com.furelise.skillmanagement.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AdminController endpoints:
 * - GET /api/v1/admin/get-users (TC-ADM-GET-001 -> TC-ADM-GET-003)
 * - DELETE /api/v1/admin/delete-user/{email} (TC-ADM-DEL-001 -> TC-ADM-DEL-004)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User normalUser;
    private String adminToken;
    private String normalUserToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = User.builder()
                .name("Admin Boss")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin12345"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        normalUser = User.builder()
                .name("Normal Worker")
                .email("worker@example.com")
                .password(passwordEncoder.encode("worker12345"))
                .role(Role.USER)
                .build();
        userRepository.save(normalUser);
        normalUserToken = jwtService.generateToken(normalUser);
    }

    @Nested
    @DisplayName("GET /api/v1/admin/get-users")
    class GetAllUsersTests {

        @Test
        @DisplayName("TC-ADM-GET-001: Admin gets all users successfully with ROLE_ADMIN (no password in response)")
        void shouldReturnAllUsersForAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/get-users")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].email", anyOf(is("admin@example.com"), is("worker@example.com"))))
                    .andExpect(jsonPath("$[0].password").doesNotExist())
                    .andExpect(jsonPath("$[1].password").doesNotExist());
        }

        @Test
        @DisplayName("TC-ADM-GET-002: Regular user (ROLE_USER) is forbidden from getting all users (403)")
        void shouldForbidRegularUserFromGettingAllUsers() throws Exception {
            mockMvc.perform(get("/api/v1/admin/get-users")
                            .header("Authorization", "Bearer " + normalUserToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status", is(403)))
                    .andExpect(jsonPath("$.message", is("Bạn không có quyền thực hiện thao tác này.")));
        }

        @Test
        @DisplayName("TC-ADM-GET-003: Unauthenticated request to get-users is rejected (401/403)")
        void shouldRejectUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/api/v1/admin/get-users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/delete-user/{email}")
    class DeleteUserTests {

        @Test
        @DisplayName("TC-ADM-DEL-001: Admin deletes user successfully by email (200 OK)")
        void shouldDeleteUserSuccessfullyForAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/delete-user/worker@example.com")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User deleted successfully."));

            assertThat(userRepository.findByEmail("worker@example.com")).isEmpty();
            assertThat(userRepository.findByEmail("admin@example.com")).isPresent();
        }

        @Test
        @DisplayName("TC-ADM-DEL-002: Regular user (ROLE_USER) is forbidden from deleting a user (403)")
        void shouldForbidRegularUserFromDeletingUser() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/delete-user/admin@example.com")
                            .header("Authorization", "Bearer " + normalUserToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status", is(403)))
                    .andExpect(jsonPath("$.message", is("Bạn không có quyền thực hiện thao tác này.")));

            assertThat(userRepository.findByEmail("admin@example.com")).isPresent();
        }

        @Test
        @DisplayName("TC-ADM-DEL-003: Admin deletes non-existent user returns 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/delete-user/nonexistent@example.com")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", is("User not found with email: nonexistent@example.com")));
        }

        @Test
        @DisplayName("TC-ADM-DEL-004: Unauthenticated request to delete-user is rejected (401/403)")
        void shouldRejectUnauthenticatedDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/delete-user/worker@example.com")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
