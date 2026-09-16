package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillCategory;
import com.furelise.skillmanagement.model.SkillVersion;
import com.furelise.skillmanagement.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class SkillDto {

    public record AuthorInfo(
            Long id,
            String name,
            String email,
            String avatarUrl
    ) {
        public static AuthorInfo from(User user) {
            if (user == null) return null;
            return new AuthorInfo(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getAvatarUrl()
            );
        }
    }

    public record SkillCategoryInfo(
            Long id,
            String name,
            String icon,
            boolean isPrimary
    ) {
        public static SkillCategoryInfo from(SkillCategory category, boolean isPrimary) {
            return new SkillCategoryInfo(
                    category.getId(),
                    category.getName(),
                    category.getIcon(),
                    isPrimary
            );
        }
    }

    public record SkillUploadRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must not exceed 255 characters")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            List<Long> categoryIds,

            Long primaryCategoryId,

            List<String> tagNames,

            String changelog
    ) {}

    public record SkillUpdateRequest(
            @Size(max = 255, message = "Title must not exceed 255 characters")
            String title,

            String description,

            List<Long> categoryIds,

            Long primaryCategoryId,

            List<String> tagNames,

            String changelog
    ) {}

    public record SkillResponse(
            Long id,
            String title,
            String description,
            List<SkillCategoryInfo> categories,
            List<String> tags,
            AuthorInfo author,
            String status,
            Integer currentVersion,
            String fileName,
            Integer usageCount,
            BigDecimal averageRating,
            Integer ratingCount,
            Long mergedIntoId,
            String deprecatedReason,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static SkillResponse from(Skill skill, List<SkillCategoryInfo> categories, List<String> tags) {
            return new SkillResponse(
                    skill.getId(),
                    skill.getTitle(),
                    skill.getDescription(),
                    categories,
                    tags,
                    AuthorInfo.from(skill.getAuthor()),
                    skill.getStatus(),
                    skill.getCurrentVersion(),
                    skill.getFileName(),
                    skill.getUsageCount(),
                    skill.getAverageRating(),
                    skill.getRatingCount(),
                    skill.getMergedInto() != null ? skill.getMergedInto().getId() : null,
                    skill.getDeprecatedReason(),
                    skill.getCreatedAt(),
                    skill.getUpdatedAt()
            );
        }
    }

    public record SkillVersionResponse(
            Long id,
            Integer version,
            String fileName,
            String changelog,
            AuthorInfo createdBy,
            ZonedDateTime createdAt
    ) {
        public static SkillVersionResponse from(SkillVersion version) {
            return new SkillVersionResponse(
                    version.getId(),
                    version.getVersion(),
                    version.getFileName(),
                    version.getChangelog(),
                    AuthorInfo.from(version.getCreatedBy()),
                    version.getCreatedAt()
            );
        }
    }
}
