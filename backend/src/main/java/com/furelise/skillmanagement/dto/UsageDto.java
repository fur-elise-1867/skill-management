package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.ImpactRecord;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class UsageDto {

    public record UsageRecordResponse(
            Long skillId,
            boolean newlyRecorded,
            Long totalUsageCount,
            String message
    ) {}

    public record ImpactRecordRequest(
            @NotBlank(message = "Reference code (project/task ID) is required")
            @Size(max = 50, message = "Reference code must not exceed 50 characters")
            String referenceCode,

            @NotNull(message = "Effectiveness score is required")
            @Min(value = 1, message = "Effectiveness score must be at least 1")
            @Max(value = 5, message = "Effectiveness score must be at most 5")
            Integer effectivenessScore,

            BigDecimal estimatedMmSaved,

            @Size(max = 1000, message = "Note must not exceed 1000 characters")
            String note
    ) {}

    public record ImpactRecordResponse(
            Long id,
            Long skillId,
            Long userId,
            String userName,
            String referenceCode,
            Integer effectivenessScore,
            BigDecimal estimatedMmSaved,
            String note,
            ZonedDateTime createdAt
    ) {
        public static ImpactRecordResponse from(ImpactRecord record) {
            String userName = record.getUser() != null
                    ? (record.getUser().getName() != null ? record.getUser().getName() : record.getUser().getEmail())
                    : "Anonymous";
            Long userId = record.getUser() != null ? record.getUser().getId() : null;

            return new ImpactRecordResponse(
                    record.getId(),
                    record.getSkill().getId(),
                    userId,
                    userName,
                    record.getReferenceCode(),
                    record.getEffectivenessScore(),
                    record.getEstimatedMmSaved(),
                    record.getNote(),
                    record.getCreatedAt()
            );
        }
    }
}
