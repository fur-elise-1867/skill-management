package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.CategoryDto.CategoryCreateRequest;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryResponse;
import com.furelise.skillmanagement.dto.CategoryDto.CategoryUpdateRequest;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.CategoryService;
import com.furelise.skillmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(categoryService.updateCategory(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        User user = userService.getAuthenticatedUser();
        categoryService.deleteCategory(id, user);
        return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công."));
    }
}
