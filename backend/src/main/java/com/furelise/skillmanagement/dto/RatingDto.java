package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.SkillRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class RatingDto {

    public record RatingRequest(
            @NotNull(message = "Rating is required")
            @Min(value = 1, message = "Rating must be at least 1")
            @Max(value = 5, message = "Rating must be at most 5")
            Integer rating
    ) {}

    public record RatingResponse(
            Long id,
            Long skillId,
            Long userId,
            String userName,
            Integer rating,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static RatingResponse from(SkillRating rating) {
            return new RatingResponse(
                    rating.getId(),
                    rating.getSkill().getId(),
                    rating.getUser().getId(),
                    rating.getUser().getName() != null ? rating.getUser().getName() : rating.getUser().getEmail(),
                    rating.getRating(),
                    rating.getCreatedAt(),
                    rating.getUpdatedAt()
            );
        }
    }
}
