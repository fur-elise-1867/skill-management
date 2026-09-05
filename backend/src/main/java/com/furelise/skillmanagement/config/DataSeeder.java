package com.furelise.skillmanagement.config;

import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes default roles on system startup if not present.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Bảng role chưa có dữ liệu, tiến hành khởi tạo danh sách vai trò mặc định...");
            roleRepository.saveAll(List.of(
                    Role.builder().name("ADMIN").description("Quản trị viên hệ thống").build(),
                    Role.builder().name("EDITOR").description("Biên tập viên nội dung").build(),
                    Role.builder().name("USER").description("Người dùng tiêu chuẩn").build()
            ));
            log.info("Khởi tạo vai trò mặc định hoàn tất: ADMIN, EDITOR, USER.");
        }
    }
}
