package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.dto.SkillDto.AuthorInfo;
import com.furelise.skillmanagement.dto.SkillDto.SkillCategoryInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public class ApprovalDto {

    public record RejectRequest(
            @NotBlank(message = "Rejection reason is required")
            String reason
    ) {}

    public record MergeSkillsRequest(
            @NotNull(message = "Source skill ID is required")
            Long sourceSkillId,

            @NotNull(message = "Target skill ID is required")
            Long targetSkillId,

            @NotBlank(message = "Merge reason is required")
            String reason
    ) {}

    public record DeprecateRequest(
            @NotBlank(message = "Deprecation reason is required")
            String reason
    ) {}

    public record PendingSkillResponse(
            Long id,
            String title,
            String description,
            AuthorInfo author,
            Integer currentVersion,
            String fileName,
            List<SkillCategoryInfo> categories,
            List<String> tags,
            boolean securityScanFlag,
            String securityScanDetail,
            ZonedDateTime createdAt
    ) {}
}
