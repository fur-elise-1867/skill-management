package com.furelise.skillmanagement.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatsDto {

    public record CategoryStat(
            Long categoryId,
            String categoryName,
            long skillCount
    ) {}

    public record DashboardStatsResponse(
            long totalSkills,
            Map<String, Long> skillsByStatus,
            long newThisWeek,
            long totalUsage,
            BigDecimal totalMmSavedThisMonth,
            List<CategoryStat> skillsPerCategory
    ) {}

    public record SkillLeaderboardItem(
            Long id,
            String title,
            String authorName,
            Integer currentVersion,
            Long countValue,
            BigDecimal ratingValue
    ) {}

    public record SkillImpactLeaderboardItem(
            Long id,
            String title,
            String authorName,
            BigDecimal totalMmSaved,
            Long recordCount
    ) {}

    public record ContributorLeaderboardItem(
            Long userId,
            String userName,
            String userAvatarUrl,
            long publishedSkillCount,
            long totalUsageCount
    ) {}

    public record LeaderboardResponse(
            List<SkillLeaderboardItem> topUsedSkills,
            List<SkillLeaderboardItem> topRatedSkills,
            List<SkillImpactLeaderboardItem> topImpactSkills,
            List<ContributorLeaderboardItem> topContributors
    ) {}

    public record MySkillsStatsResponse(
            long mySkillsCount,
            long myPublishedCount,
            long totalUsageOfMySkills,
            long skillsUsedByMe,
            BigDecimal totalMmSavedByMe
    ) {}
}
