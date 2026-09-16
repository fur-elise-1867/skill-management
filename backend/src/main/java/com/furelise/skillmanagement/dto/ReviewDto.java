package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.SkillReview;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public class ReviewDto {

    public record ReviewCreateRequest(
            @NotBlank(message = "Review content is required")
            @Size(max = 2000, message = "Review content must not exceed 2000 characters")
            String content
    ) {}

    public record ReviewUpdateRequest(
            @NotBlank(message = "Review content is required")
            @Size(max = 2000, message = "Review content must not exceed 2000 characters")
            String content
    ) {}

    public record ReviewReportRequest(
            @NotBlank(message = "Report category is required")
            String category,

            @Size(max = 500, message = "Report detail must not exceed 500 characters")
            String detail
    ) {}

    public record ReviewResponse(
            Long id,
            Long skillId,
            Long userId,
            String userName,
            String userAvatarUrl,
            String content,
            Integer helpfulCount,
            String reportCategory,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static ReviewResponse from(SkillReview review) {
            String userName = review.getUser() != null
                    ? (review.getUser().getName() != null ? review.getUser().getName() : review.getUser().getEmail())
                    : "Anonymous";
            String userAvatar = review.getUser() != null ? review.getUser().getAvatarUrl() : null;
            Long userId = review.getUser() != null ? review.getUser().getId() : null;

            return new ReviewResponse(
                    review.getId(),
                    review.getSkill().getId(),
                    userId,
                    userName,
                    userAvatar,
                    review.getContent(),
                    review.getHelpfulCount(),
                    review.getReportCategory(),
                    review.getCreatedAt(),
                    review.getUpdatedAt()
            );
        }
    }
}
