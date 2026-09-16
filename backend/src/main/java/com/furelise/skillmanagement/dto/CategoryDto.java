package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public class CategoryDto {

    public record CategoryResponse(
            Long id,
            String name,
            String description,
            String icon,
            ZonedDateTime createdAt
    ) {
        public static CategoryResponse from(SkillCategory category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.getIcon(),
                    category.getCreatedAt()
            );
        }
    }

    public record CategoryCreateRequest(
            @NotBlank(message = "Category name is required")
            @Size(max = 100, message = "Category name must not exceed 100 characters")
            String name,

            String description,

            @Size(max = 50, message = "Icon must not exceed 50 characters")
            String icon
    ) {}

    public record CategoryUpdateRequest(
            @NotBlank(message = "Category name is required")
            @Size(max = 100, message = "Category name must not exceed 100 characters")
            String name,

            String description,

            @Size(max = 50, message = "Icon must not exceed 50 characters")
            String icon
    ) {}
}
