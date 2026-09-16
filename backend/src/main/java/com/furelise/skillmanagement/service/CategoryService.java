package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.CategoryDto.CategoryCreateRequest;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryResponse;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryUpdateRequest;
import com.furelise.skillmanagement.exception.BadRequestException;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.SkillCategory;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillCategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final SkillCategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @PostConstruct
    @Transactional
    public void seedDefaultCategories() {
        seedCategoryIfAbsent("Development", "💻", "Coding, debugging, refactoring");
        seedCategoryIfAbsent("Testing & QA", "🧪", "Test case, automation, review");
        seedCategoryIfAbsent("Business Analysis", "📋", "BA document, requirements");
        seedCategoryIfAbsent("Project Management", "📊", "Jira, Confluence, reporting");
        seedCategoryIfAbsent("Data Analysis", "📈", "Excel, SQL, dashboard");
        seedCategoryIfAbsent("DevOps", "🔧", "CI/CD, Docker, deployment");
        seedCategoryIfAbsent("Documentation", "📝", "Writing, translation");
        seedCategoryIfAbsent("Other", "📦", "Các skill khác");
    }

    private void seedCategoryIfAbsent(String name, String icon, String description) {
        if (categoryRepository.findByName(name).isEmpty()) {
            SkillCategory category = new SkillCategory();
            category.setName(name);
            category.setIcon(icon);
            category.setDescription(description);
            categoryRepository.save(category);
            log.info("Seeded default skill category: {}", name);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillCategory getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request, User user) {
        if (categoryRepository.findByName(request.name().trim()).isPresent()) {
            throw new BadRequestException("Danh mục với tên này đã tồn tại: " + request.name());
        }

        SkillCategory category = new SkillCategory();
        category.setName(request.name().trim());
        category.setDescription(request.description());
        category.setIcon(request.icon());

        SkillCategory saved = categoryRepository.save(category);
        auditLogService.log(user, "CATEGORY_CREATE", "CATEGORY", saved.getId(), "Tạo danh mục mới: " + saved.getName());

        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request, User user) {
        SkillCategory category = getCategoryEntity(id);

        String newName = request.name().trim();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.findByName(newName).isPresent()) {
            throw new BadRequestException("Danh mục với tên này đã tồn tại: " + newName);
        }

        category.setName(newName);
        category.setDescription(request.description());
        category.setIcon(request.icon());

        SkillCategory updated = categoryRepository.save(category);
        auditLogService.log(user, "CATEGORY_UPDATE", "CATEGORY", updated.getId(), "Cập nhật danh mục: " + updated.getName());

        return CategoryResponse.from(updated);
    }

    @Transactional
    public void deleteCategory(Long id, User user) {
        SkillCategory category = getCategoryEntity(id);
        if (!category.getSkills().isEmpty()) {
            throw new BadRequestException("Không thể xóa danh mục đang chứa kỹ năng (skills).");
        }

        categoryRepository.delete(category);
        auditLogService.log(user, "CATEGORY_DELETE", "CATEGORY", id, "Xóa danh mục: " + category.getName());
    }
}
