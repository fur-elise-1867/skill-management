# Kế Hoạch Triển Khai: Nâng Cấp Quản Trị Người Dùng & Phân Quyền (RBAC)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Triển khai nâng cấp hệ thống phân quyền động (Role trong DB với ADMIN, EDITOR, USER), mở rộng tính năng quản trị (thêm tài khoản, đổi role có bảo vệ Admin, bật/tắt active, reset password ngẫu nhiên), trang thông tin cá nhân (đổi avatar, thông tin, đổi mật khẩu, tự xóa tài khoản), và điều hướng đăng ký sang trang đăng nhập.

**Architecture:** 
- Backend: Spring Boot 3 + Spring Data JPA + Spring Security. Bảng `role` độc lập với khởi tạo dữ liệu tự động (`DataSeeder`); bảng `_user` liên kết ManyToOne tới `role`, bổ sung `enabled` và `avatar_url`. Phục vụ ảnh avatar qua `WebMvcConfigurer` tại `/uploads/avatars/**`.
- Frontend: Vue 3 + Pinia + Vue Router + Element Plus. Bổ sung trang `AccountInfoView.vue`, nâng cấp `AdminView.vue`, cập nhật luồng đăng ký trong `RegisterView.vue` và `stores/auth.ts`.

**Tech Stack:** Java 17+, Spring Boot 3, Spring Security 6, JPA / Hibernate, H2 / PostgreSQL, TypeScript, Vue 3, Pinia, Element Plus, Vite, Vitest.

**Spec:** [`docs/superpowers/specs/2026-09-05-user-management-rbac-design.md`](file:///d:/Documents/Git%20Project/skill-management/docs/superpowers/specs/2026-09-05-user-management-rbac-design.md)

## Global Constraints
- Java package gốc: `com.furelise.skillmanagement`
- Đường dẫn upload file avatar: `uploads/avatars/`
- Dung lượng ảnh tối đa: 5MB; định dạng: JPG, JPEG, PNG, WEBP
- Tiền tố authority Spring Security: `ROLE_`
- Mật khẩu tối thiểu: 8 ký tự; mật khẩu ngẫu nhiên khi reset: 10 ký tự gồm hoa, thường, số, ký tự đặc biệt
- Tuyệt đối không cho phép hạ cấp role của tài khoản `ADMIN`
- Tuyệt đối không cho phép Admin tự vô hiệu hóa (`enabled = false`) tài khoản của chính mình
- Tuyệt đối không cho phép Admin tự xóa tài khoản của chính mình
- Đăng ký thành công phải redirect về `/login`, không lưu token

---

### Task 1: Khởi Tạo Entity Role, Cập Nhật Entity User & Seeder Dữ Liệu Ban Đầu

**Files:**
- Create: `backend/src/main/java/com/furelise/skillmanagement/model/Role.java` (chuyển đổi từ enum sang JPA Entity)
- Modify: `backend/src/main/java/com/furelise/skillmanagement/model/User.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/repository/RoleRepository.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/config/DataSeeder.java`
- Test: `backend/src/test/java/com/furelise/skillmanagement/repository/RoleRepositoryTest.java`

**Interfaces:**
- Consumes: JPA Entity & Repository
- Produces: 
  - `Role`: Entity `id`, `name`, `description`
  - `RoleRepository`: `Optional<Role> findByName(String name)`
  - `User`: Có các trường `role` (`Role`), `enabled` (`boolean`), `avatarUrl` (`String`)
  - `DataSeeder`: Tự động chèn 3 roles `ADMIN`, `EDITOR`, `USER` nếu bảng rỗng khi khởi động

- [ ] **Step 1: Viết test cho RoleRepository**
```java
package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldSaveAndFindRoleByName() {
        Role role = Role.builder()
                .name("ADMIN")
                .description("Quản trị viên")
                .build();
        roleRepository.save(role);

        var found = roleRepository.findByName("ADMIN");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ADMIN");
    }
}
```

- [ ] **Step 2: Chạy test để xác nhận lỗi biên dịch**
Chạy: `./mvnw test -Dtest=RoleRepositoryTest` (trong thư mục `backend`)
Kỳ vọng: Lỗi biên dịch vì `RoleRepository` chưa tồn tại và `Role` đang là enum.

- [ ] **Step 3: Triển khai Role Entity, RoleRepository, cập nhật User Entity và DataSeeder**
1. `Role.java`:
```java
package com.furelise.skillmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;
}
```

2. `RoleRepository.java`:
```java
package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
```

3. Cập nhật `User.java`:
Thay đổi trường `role` từ enum sang `@ManyToOne Role role`, thêm `enabled` và `avatarUrl`:
```java
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
```

4. `DataSeeder.java`:
```java
package com.furelise.skillmanagement.config;

import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Khởi tạo danh sách vai trò mặc định...");
            roleRepository.saveAll(List.of(
                    Role.builder().name("ADMIN").description("Quản trị viên hệ thống").build(),
                    Role.builder().name("EDITOR").description("Biên tập viên nội dung").build(),
                    Role.builder().name("USER").description("Người dùng tiêu chuẩn").build()
            ));
            log.info("Khởi tạo vai trò hoàn tất.");
        }
    }
}
```

- [ ] **Step 4: Chạy lại test để xác nhận PASS**
Chạy: `./mvnw test -Dtest=RoleRepositoryTest` (trong thư mục `backend`)
Kỳ vọng: Test PASS.

- [ ] **Step 5: Commit**
```bash
git add backend/src/main/java/com/furelise/skillmanagement/model/Role.java
git add backend/src/main/java/com/furelise/skillmanagement/model/User.java
git add backend/src/main/java/com/furelise/skillmanagement/repository/RoleRepository.java
git add backend/src/main/java/com/furelise/skillmanagement/config/DataSeeder.java
git add backend/src/test/java/com/furelise/skillmanagement/repository/RoleRepositoryTest.java
git commit -m "feat(backend): implement dynamic Role entity, update User entity and seed initial roles"
```

---

### Task 2: Cấu Hình Lưu Trữ Avatar & Phục Vụ Tệp Tĩnh

**Files:**
- Create: `backend/src/main/java/com/furelise/skillmanagement/config/WebMvcConfig.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/service/FileStorageService.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/service/impl/FileStorageServiceImpl.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/config/SecurityConfig.java`
- Test: `backend/src/test/java/com/furelise/skillmanagement/service/FileStorageServiceTest.java`

**Interfaces:**
- Consumes: `MultipartFile`
- Produces: 
  - `String storeAvatar(Long userId, MultipartFile file)`: Lưu ảnh vào `uploads/avatars/`, xóa ảnh cũ nếu có, trả về URL tương đối `/uploads/avatars/...`
  - `void deleteAvatar(String avatarUrl)`: Xóa file ảnh khỏi đĩa
  - `WebMvcConfig`: Phục vụ file tĩnh từ đường dẫn file vật lý
  - `SecurityConfig`: Cấp quyền `permitAll()` cho `/uploads/**`

- [ ] **Step 1: Viết test cho FileStorageService**
```java
package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        fileStorageService = new FileStorageServiceImpl(tempDir.toString());
    }

    @Test
    void shouldStoreValidAvatar() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "test.png", "image/png", "fake image content".getBytes()
        );

        String url = fileStorageService.storeAvatar(1L, file, null);
        assertThat(url).startsWith("/uploads/avatars/avatar_1_");
        assertThat(url).endsWith(".png");
    }

    @Test
    void shouldRejectInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "test.exe", "application/octet-stream", "bad content".getBytes()
        );

        assertThatThrownBy(() -> fileStorageService.storeAvatar(1L, file, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Chạy test để kiểm tra FAIL**
Chạy: `./mvnw test -Dtest=FileStorageServiceTest`
Kỳ vọng: Lỗi biên dịch do chưa có `FileStorageService`.

- [ ] **Step 3: Triển khai FileStorageService và WebMvcConfig**
1. `FileStorageService.java`:
```java
package com.furelise.skillmanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeAvatar(Long userId, MultipartFile file, String oldAvatarUrl);
    void deleteAvatar(String avatarUrl);
}
```

2. `FileStorageServiceImpl.java`:
```java
package com.furelise.skillmanagement.service.impl;

import com.furelise.skillmanagement.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads/avatars/}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ file: " + uploadDirPath, e);
        }
    }

    @Override
    public String storeAvatar(Long userId, MultipartFile file, String oldAvatarUrl) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File upload không được để trống.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dung lượng ảnh tối đa là 5MB.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không hợp lệ. Chỉ chấp nhận JPG, PNG, WEBP.");
        }

        // Xóa file cũ nếu có
        deleteAvatar(oldAvatarUrl);

        String newFilename = "avatar_" + userId + "_" + System.currentTimeMillis() + "." + extension;
        Path targetPath = this.uploadDir.resolve(newFilename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file avatar.", e);
        }

        return "/uploads/avatars/" + newFilename;
    }

    @Override
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith("/uploads/avatars/")) {
            return;
        }
        String filename = avatarUrl.substring("/uploads/avatars/".length());
        Path filePath = this.uploadDir.resolve(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Không thể xóa file avatar cũ: {}", filePath, e);
        }
    }
}
```

3. `WebMvcConfig.java`:
```java
package com.furelise.skillmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/avatars/}")
    private String uploadDirPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + path.toString() + "/");
    }
}
```

4. Cập nhật `SecurityConfig.java`: thêm `"/uploads/**"` vào `permitAll()`.

- [ ] **Step 4: Chạy test để xác nhận PASS**
Chạy: `./mvnw test -Dtest=FileStorageServiceTest`
Kỳ vọng: Test PASS.

- [ ] **Step 5: Commit**
```bash
git add backend/src/main/java/com/furelise/skillmanagement/config/WebMvcConfig.java
git add backend/src/main/java/com/furelise/skillmanagement/config/SecurityConfig.java
git add backend/src/main/java/com/furelise/skillmanagement/service/FileStorageService.java
git add backend/src/main/java/com/furelise/skillmanagement/service/impl/FileStorageServiceImpl.java
git add backend/src/test/java/com/furelise/skillmanagement/service/FileStorageServiceTest.java
git commit -m "feat(backend): implement avatar file storage and static resource serving"
```

---

### Task 3: Triển Khai DTOs & API Quản Lý Tài Khoản Cá Nhân (User Profile, Change Password, Upload Avatar, Delete Account)

**Files:**
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/UpdateProfileRequest.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/ChangePasswordRequest.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/dto/UserResponse.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/service/UserService.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/service/impl/UserServiceImpl.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/controller/UserController.java`
- Test: `backend/src/test/java/com/furelise/skillmanagement/service/UserServiceTest.java`

**Interfaces:**
- Consumes: Authenticated user context, `FileStorageService`, `PasswordEncoder`
- Produces:
  - `PUT /api/v1/user/profile`: Cập nhật `name`, `gender`, `mobile`
  - `PUT /api/v1/user/change-password`: Đổi mật khẩu của bản thân
  - `POST /api/v1/user/avatar`: Upload avatar
  - `DELETE /api/v1/user/account`: Tự xóa tài khoản (chặn nếu vai trò là ADMIN)

- [ ] **Step 1: Viết test cho UserService**
```java
package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ChangePasswordRequest;
import com.furelise.skillmanagement.dto.UpdateProfileRequest;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
```

- [ ] **Step 2: Chạy test để kiểm tra FAIL**
Chạy: `./mvnw test -Dtest=UserServiceTest`
Kỳ vọng: FAIL vì chưa có method `deleteOwnAccount`.

- [ ] **Step 3: Triển khai các DTOs, UserService và UserController**
1. Cập nhật `UserResponse.java`: Bổ sung `Role` (DTO hoặc object name/description), `enabled`, `avatarUrl`.
2. `UpdateProfileRequest.java`:
```java
package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên không được để trống") String name,
        String gender,
        String mobile
) {}
```
3. `ChangePasswordRequest.java`:
```java
package com.furelise.skillmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại không được để trống") String oldPassword,
        @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 8, message = "Mật khẩu mới phải có tối thiểu 8 ký tự") String newPassword
) {}
```
4. `UserService.java` & `UserServiceImpl.java`: Triển khai các method `updateProfile`, `changePassword`, `updateAvatar`, `deleteOwnAccount`.
5. `UserController.java`: Thêm các endpoint `PUT /profile`, `PUT /change-password`, `POST /avatar`, `DELETE /account`.

- [ ] **Step 4: Chạy test để xác nhận PASS**
Chạy: `./mvnw test -Dtest=UserServiceTest`
Kỳ vọng: Test PASS.

- [ ] **Step 5: Commit**
```bash
git add backend/src/main/java/com/furelise/skillmanagement/dto/
git add backend/src/main/java/com/furelise/skillmanagement/service/UserService.java
git add backend/src/main/java/com/furelise/skillmanagement/service/impl/UserServiceImpl.java
git add backend/src/main/java/com/furelise/skillmanagement/controller/UserController.java
git add backend/src/test/java/com/furelise/skillmanagement/service/UserServiceTest.java
git commit -m "feat(backend): implement user profile, password change, avatar upload, and self-delete"
```

---

### Task 4: Triển Khai DTOs & API Quản Trị Viên (Admin Management, Role Protection, Status Switch, Password Reset)

**Files:**
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/CreateUserRequest.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/UpdateRoleRequest.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/UpdateStatusRequest.java`
- Create: `backend/src/main/java/com/furelise/skillmanagement/dto/ResetPasswordResponse.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/service/AdminService.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/service/impl/AdminServiceImpl.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/controller/AdminController.java`
- Test: `backend/src/test/java/com/furelise/skillmanagement/service/AdminServiceTest.java`

**Interfaces:**
- Consumes: `UserRepository`, `RoleRepository`, `PasswordEncoder`, `FileStorageService`
- Produces:
  - `GET /api/v1/admin/roles`: Danh sách tất cả roles
  - `POST /api/v1/admin/create-user`: Tạo tài khoản mới
  - `PUT /api/v1/admin/users/{id}/role`: Đổi vai trò (chặn hạ cấp nếu role mục tiêu là `ADMIN`)
  - `PUT /api/v1/admin/users/{id}/status`: Bật/tắt active (chặn tự khóa chính mình)
  - `POST /api/v1/admin/users/{id}/reset-password`: Sinh mật khẩu ngẫu nhiên an toàn, trả về plain text cho admin
  - `DELETE /api/v1/admin/delete-user/{id}`: Xóa tài khoản (chặn tự xóa chính mình)

- [ ] **Step 1: Viết test cho AdminService**
```java
package com.furelise.skillmanagement.service;

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
    void shouldRejectAdminSelfDeactivation() {
        User currentAdmin = User.builder().id(1L).email("admin@test.com").build();
        User targetUser = User.builder().id(1L).email("admin@test.com").enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        assertThatThrownBy(() -> adminService.updateUserStatus(1L, false, currentAdmin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bạn không thể tự khóa tài khoản của chính mình");
    }
}
```

- [ ] **Step 2: Chạy test để kiểm tra FAIL**
Chạy: `./mvnw test -Dtest=AdminServiceTest`
Kỳ vọng: FAIL vì chưa có method `updateUserRole` và `updateUserStatus`.

- [ ] **Step 3: Triển khai các DTOs, logic AdminService và AdminController**
1. Các DTOs:
   - `CreateUserRequest`: `name`, `email`, `password`, `gender`, `mobile`, `roleId`, `enabled`
   - `UpdateRoleRequest`: `roleId`
   - `UpdateStatusRequest`: `enabled`
   - `ResetPasswordResponse`: `newPassword`
2. `AdminServiceImpl.java`:
   - Hàm sinh mật khẩu ngẫu nhiên an toàn:
     ```java
     private String generateRandomPassword() {
         String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
         String lower = "abcdefghijklmnopqrstuvwxyz";
         String digits = "0123456789";
         String special = "!@#$%^&*";
         // Kết hợp và trộn ngẫu nhiên 10 ký tự
         ...
     }
     ```
   - Logic `updateUserRole`: Kiểm tra nếu `targetUser.getRole().getName().equalsIgnoreCase("ADMIN")` thì ném `IllegalArgumentException`.
   - Logic `updateUserStatus`: Kiểm tra nếu `targetUser.getId().equals(currentAdmin.getId()) && !enabled` thì ném `IllegalArgumentException`.
   - Logic `resetPassword`: Sinh password ngẫu nhiên, mã hóa BCrypt, cập nhật user, trả về `ResetPasswordResponse`.
3. `AdminController.java`: Thêm đầy đủ các endpoint với `@Secured("ROLE_ADMIN")`.

- [ ] **Step 4: Chạy test để xác nhận PASS**
Chạy: `./mvnw test -Dtest=AdminServiceTest`
Kỳ vọng: Test PASS.

- [ ] **Step 5: Commit**
```bash
git add backend/src/main/java/com/furelise/skillmanagement/dto/
git add backend/src/main/java/com/furelise/skillmanagement/service/AdminService.java
git add backend/src/main/java/com/furelise/skillmanagement/service/impl/AdminServiceImpl.java
git add backend/src/main/java/com/furelise/skillmanagement/controller/AdminController.java
git add backend/src/test/java/com/furelise/skillmanagement/service/AdminServiceTest.java
git commit -m "feat(backend): implement admin endpoints with role protection, status switch, and password reset"
```

---

### Task 5: Cập Nhật Luồng Đăng Ký & Xử Lý Đăng Nhập Tài Khoản Inactive

**Files:**
- Modify: `backend/src/main/java/com/furelise/skillmanagement/service/impl/AuthenticationServiceImpl.java`
- Modify: `backend/src/main/java/com/furelise/skillmanagement/exception/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/furelise/skillmanagement/service/AuthenticationServiceTest.java`

**Interfaces:**
- Consumes: `RegisterRequest`, `AuthenticationRequest`, `RoleRepository`
- Produces:
  - `register`: Tự động tìm role `USER` từ DB và gán cho user mới; `enabled = true`.
  - `authenticate`: Khi tài khoản bị inactive (`enabled = false`), Spring Security ném `DisabledException`. `GlobalExceptionHandler` bắt và trả về HTTP 403 Forbidden với thông báo: *"Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt. Vui lòng liên hệ Quản trị viên."*

- [ ] **Step 1: Viết test cho AuthenticationService**
Kiểm tra đăng ký gán đúng role `USER` từ repository và bắt `DisabledException` khi tài khoản inactive.

- [ ] **Step 2: Chạy test để kiểm tra FAIL**
Chạy: `./mvnw test -Dtest=AuthenticationServiceTest`

- [ ] **Step 3: Cập nhật AuthenticationServiceImpl và GlobalExceptionHandler**
1. Trong `AuthenticationServiceImpl.java`: Tìm `Role userRole = roleRepository.findByName("USER").orElseThrow(...)` để gán cho user mới.
2. Trong `GlobalExceptionHandler.java`:
```java
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt. Vui lòng liên hệ Quản trị viên.",
                        Instant.now()
                ));
    }
```

- [ ] **Step 4: Chạy lại toàn bộ test Backend (`mvn test`)**
Chạy: `./mvnw test`
Kỳ vọng: Toàn bộ test suite backend PASS 100%.

- [ ] **Step 5: Commit**
```bash
git add backend/src/main/java/com/furelise/skillmanagement/service/impl/AuthenticationServiceImpl.java
git add backend/src/main/java/com/furelise/skillmanagement/exception/GlobalExceptionHandler.java
git add backend/src/test/java/com/furelise/skillmanagement/service/AuthenticationServiceTest.java
git commit -m "feat(backend): handle disabled account login and default role assignment on register"
```

---

### Task 6: Cập Nhật Type Definitions & API Services Frontend

**Files:**
- Modify: `frontend/src/types/index.ts`
- Modify: `frontend/src/services/authService.ts`
- Create: `frontend/src/services/userService.ts`
- Create: `frontend/src/services/adminService.ts`

**Interfaces:**
- Consumes: Axios API client
- Produces: 
  - `RoleResponse`: `{ id: number, name: string, description: string }`
  - `UserResponse`: `{ id: number, name: string, email: string, gender: string, mobile: string, role: RoleResponse, enabled: boolean, avatarUrl: string, createdAt: string, updatedAt: string }`
  - `userService`: `getCurrentUser()`, `updateProfile()`, `changePassword()`, `uploadAvatar()`, `deleteAccount()`
  - `adminService`: `getRoles()`, `getUsers()`, `createUser()`, `updateRole()`, `updateStatus()`, `resetPassword()`, `deleteUser()`

- [ ] **Step 1: Cập nhật `frontend/src/types/index.ts`**
Thêm các interface cho `RoleResponse`, `UpdateProfileRequest`, `ChangePasswordRequest`, `CreateUserRequest`, `ResetPasswordResponse`.

- [ ] **Step 2: Triển khai `userService.ts` và `adminService.ts`**
Định nghĩa đầy đủ các hàm gọi API tương ứng với backend.

- [ ] **Step 3: Kiểm tra biên dịch TypeScript**
Chạy: `npm run type-check` (hoặc `npm run build` trong thư mục `frontend`).
Kỳ vọng: Không có lỗi type.

- [ ] **Step 4: Commit**
```bash
git add frontend/src/types/index.ts
git add frontend/src/services/
git commit -m "feat(frontend): update types and create user and admin services"
```

---

### Task 7: Cập Nhật Luồng Đăng Ký (Register Redirect to Login)

**Files:**
- Modify: `frontend/src/stores/auth.ts`
- Modify: `frontend/src/views/RegisterView.vue`

**Interfaces:**
- Consumes: `authService.register`
- Produces:
  - Khi đăng ký thành công: Hiển thị `ElMessage.success("Đăng ký tài khoản thành công! Vui lòng đăng nhập bằng tài khoản vừa tạo.")` và điều hướng về `/login`.
  - Không lưu token, không tự động chuyển vào `/dashboard`.

- [ ] **Step 1: Sửa hàm `register` trong `auth.ts`**
Bỏ việc lưu `token.value = response.token` và `router.push('/dashboard')`. Đổi thành gọi API đăng ký và điều hướng sang `/login`.

- [ ] **Step 2: Cập nhật thông báo và form trong `RegisterView.vue`**
Đảm bảo thông báo thành công hiển thị rõ ràng và chuyển hướng đúng về `/login`.

- [ ] **Step 3: Kiểm tra hoạt động bằng manual test hoặc unit test**
Xác nhận sau khi register, người dùng ở tại trang `/login` và chưa có token trong `localStorage`.

- [ ] **Step 4: Commit**
```bash
git add frontend/src/stores/auth.ts
git add frontend/src/views/RegisterView.vue
git commit -m "feat(frontend): redirect to login after successful registration"
```

---

### Task 8: Triển Khai Trang Thông Tin Tài Khoản (AccountInfoView.vue)

**Files:**
- Create: `frontend/src/views/AccountInfoView.vue`
- Modify: `frontend/src/router/index.ts` (thêm route `/account`)

**Interfaces:**
- Consumes: `userService`, `authStore`
- Produces:
  - Trang `/account` với 3 thẻ:
    1. Thông tin cá nhân & Avatar upload (preview, upload, đổi name/gender/mobile)
    2. Đổi mật khẩu (old password, new password, confirm password)
    3. Danger Zone: Tự xóa tài khoản (ẩn/disabled nếu là Admin; hiển thị cho Editor/User)

- [ ] **Step 1: Tạo `AccountInfoView.vue` với giao diện Element Plus**
Sử dụng các component `el-card`, `el-avatar`, `el-upload`, `el-form`, `el-tag`, `el-button`. Thêm styling CSS theo design system của dự án.

- [ ] **Step 2: Đăng ký route `/account` trong `frontend/src/router/index.ts`**
Thêm route `{ path: '/account', name: 'account', component: () => import('@/views/AccountInfoView.vue'), meta: { requiresAuth: true } }`.

- [ ] **Step 3: Kiểm tra build TypeScript**
Chạy: `npm run build` trong `frontend`.
Kỳ vọng: Build thành công không có lỗi cú pháp hoặc type.

- [ ] **Step 4: Commit**
```bash
git add frontend/src/views/AccountInfoView.vue
git add frontend/src/router/index.ts
git commit -m "feat(frontend): implement account information view with avatar upload and profile management"
```

---

### Task 9: Nâng Cấp Trang Quản Trị Người Dùng (AdminView.vue)

**Files:**
- Modify: `frontend/src/views/AdminView.vue`

**Interfaces:**
- Consumes: `adminService`, `authStore`
- Produces:
  - Nút "+ Thêm tài khoản": Mở modal tạo tài khoản
  - Cột Avatar nhỏ hiển thị ảnh đại diện
  - Cột Vai trò: `el-select` chọn role động; disable nếu role là `ADMIN` kèm tooltip chú thích
  - Cột Trạng thái: `el-switch` active/inactive; disable đối với chính Admin đang login
  - Cột Hành động:
    - Nút "Reset MK": Gọi API reset password, mở Dialog hiển thị mật khẩu mới và nút Copy
    - Nút "Xóa": Xác nhận xóa tài khoản

- [ ] **Step 1: Cập nhật template và script của `AdminView.vue`**
Bổ sung các reactive state: `roles`, `createDialogVisible`, `resetDialogVisible`, `newGeneratedPassword`.
Thêm các hàm: `handleCreateUser`, `handleRoleChange`, `handleStatusChange`, `handleResetPassword`, `copyToClipboard`.

- [ ] **Step 2: Kiểm tra build Frontend**
Chạy: `npm run build` trong `frontend`.
Kỳ vọng: Build thành công.

- [ ] **Step 3: Commit**
```bash
git add frontend/src/views/AdminView.vue
git commit -m "feat(frontend): upgrade admin view with add user dialog, role switch, status toggle, and password reset"
```

---

### Task 10: Tích Hợp Header Navigation & Hiển Thị Avatar

**Files:**
- Modify: `frontend/src/views/DashboardView.vue`
- Modify: `frontend/src/views/AdminView.vue`
- Modify: `frontend/src/App.vue` (nếu có header chung)

**Interfaces:**
- Consumes: `authStore.user`
- Produces:
  - Header hiển thị avatar thu nhỏ và tên người dùng
  - Nút "Thông tin tài khoản" (`$router.push('/account')`)
  - Nút "Quản trị" (chỉ hiện khi user có vai trò `ADMIN`)

- [ ] **Step 1: Cập nhật Header trong `DashboardView.vue` và `AdminView.vue`**
Thêm avatar tròn nhỏ cạnh tên người dùng, nút điều hướng tới `/account`.

- [ ] **Step 2: Kiểm tra build Frontend**
Chạy: `npm run build` trong `frontend`.
Kỳ vọng: Build thành công 100%.

- [ ] **Step 3: Commit**
```bash
git add frontend/src/views/DashboardView.vue
git add frontend/src/views/AdminView.vue
git commit -m "feat(frontend): integrate header navigation and avatar display"
```

---

### Task 11: Kiểm Thử Toàn Diện Hệ Thống (End-to-End Verification)

**Files:**
- Toàn bộ source code Backend & Frontend

- [ ] **Step 1: Chạy toàn bộ test Backend**
Chạy: `./mvnw clean test` (trong thư mục `backend`).
Kỳ vọng: Toàn bộ unit test PASS 100%.

- [ ] **Step 2: Chạy build production Frontend**
Chạy: `npm run build` (trong thư mục `frontend`).
Kỳ vọng: File tĩnh được tạo trong `dist/` không có warning/error.

- [ ] **Step 3: Chạy ứng dụng và kiểm thử thủ công tất cả kịch bản**
1. Đăng ký tài khoản `testuser@example.com` -> Kiểm tra chuyển sang `/login`.
2. Đăng nhập với tài khoản `testuser@example.com` -> Vào Dashboard.
3. Vào trang `/account`:
   - Upload avatar -> Kiểm tra ảnh hiển thị ngay lập tức.
   - Cập nhật tên và SĐT -> Kiểm tra lưu thành công.
   - Đổi mật khẩu -> Đăng xuất và đăng nhập lại bằng mật khẩu mới.
4. Đăng nhập bằng tài khoản `ADMIN`:
   - Vào trang `/admin`.
   - Bấm "+ Thêm tài khoản": Tạo 1 tài khoản Editor.
   - Kiểm tra hàng của Admin: Dropdown role bị disable, switch trạng thái bị disable.
   - Bấm "Reset MK" tài khoản Editor -> Dialog hiện mật khẩu mới -> Bấm copy.
   - Tắt switch trạng thái của tài khoản Editor -> Đăng xuất -> Thử đăng nhập tài khoản Editor -> Báo lỗi tài khoản bị khóa.
   - Bật lại trạng thái -> Đăng nhập tài khoản Editor bằng mật khẩu vừa reset -> Thành công.
5. Đăng nhập tài khoản Editor:
   - Vào `/account` -> Thử bấm "Xoá tài khoản của tôi" -> Xác nhận xóa -> Kiểm tra bị đăng xuất và tài khoản không còn trong hệ thống.
