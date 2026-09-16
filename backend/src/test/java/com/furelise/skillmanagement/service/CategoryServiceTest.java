package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.CategoryDto.CategoryCreateRequest;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryResponse;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryUpdateRequest;
import com.furelise.skillmanagement.exception.BadRequestException;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.SkillCategory;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private SkillCategoryRepository categoryRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CategoryService categoryService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().name("ADMIN").build();
        adminUser = User.builder().id(1L).email("admin@example.com").role(role).build();
    }

    @Test
    @DisplayName("Should return all categories")
    void shouldReturnAllCategories() {
        SkillCategory c1 = new SkillCategory();
        c1.setId(1L);
        c1.setName("Development");

        SkillCategory c2 = new SkillCategory();
        c2.setId(2L);
        c2.setName("DevOps");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CategoryResponse> result = categoryService.getAllCategories();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Development");
        assertThat(result.get(1).name()).isEqualTo("DevOps");
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest("Testing", "QA & Test", "🧪");

        when(categoryRepository.findByName("Testing")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(SkillCategory.class))).thenAnswer(invocation -> {
            SkillCategory c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoryResponse response = categoryService.createCategory(request, adminUser);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Testing");
        verify(auditLogService).log(eq(adminUser), eq("CATEGORY_CREATE"), eq("CATEGORY"), eq(10L), anyString());
    }

    @Test
    @DisplayName("Should reject creating duplicate category name")
    void shouldRejectDuplicateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest("Development", "Desc", "💻");
        when(categoryRepository.findByName("Development")).thenReturn(Optional.of(new SkillCategory()));

        assertThatThrownBy(() -> categoryService.createCategory(request, adminUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategory() {
        SkillCategory existing = new SkillCategory();
        existing.setId(1L);
        existing.setName("Old Name");

        CategoryUpdateRequest request = new CategoryUpdateRequest("New Name", "New Desc", "📦");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(SkillCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse updated = categoryService.updateCategory(1L, request, adminUser);
        assertThat(updated.name()).isEqualTo("New Name");
        verify(auditLogService).log(eq(adminUser), eq("CATEGORY_UPDATE"), eq("CATEGORY"), eq(1L), anyString());
    }
}
