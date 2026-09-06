package com.furelise.skillmanagement.config;

import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.RoleRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default roles and admin user on system startup if not present.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Khoi tao 3 vai tro mac dinh neu chua co
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ADMIN").description("Quản trị viên hệ thống").build()));
        roleRepository.findByName("EDITOR").orElseGet(() ->
                roleRepository.save(Role.builder().name("EDITOR").description("Biên tập viên nội dung").build()));
        roleRepository.findByName("USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("USER").description("Người dùng tiêu chuẩn").build()));
        log.info("Khởi tạo 3 vai trò mặc định hoàn tất: ADMIN, EDITOR, USER.");

        // 2. Khoi tao tai khoan admin mac dinh neu chua co
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            log.info("Chưa có tài khoản admin mặc định, tiến hành khởi tạo admin@gmail.com...");
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("password"))
                    .role(adminRole)
                    .enabled(true)
                    .build());
            log.info("Khởi tạo tài khoản admin mặc định thành công (admin@gmail.com / password).");
        }
    }
}
