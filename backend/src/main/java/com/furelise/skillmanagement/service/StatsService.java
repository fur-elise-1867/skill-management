package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.StatsDto.CategoryStat;
import com.furelise.skillmanagement.dto.StatsDto.ContributorLeaderboardItem;
import com.furelise.skillmanagement.dto.StatsDto.DashboardStatsResponse;
import com.furelise.skillmanagement.dto.StatsDto.LeaderboardResponse;
import com.furelise.skillmanagement.dto.StatsDto.MySkillsStatsResponse;
import com.furelise.skillmanagement.dto.StatsDto.SkillImpactLeaderboardItem;
import com.furelise.skillmanagement.dto.StatsDto.SkillLeaderboardItem;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.ImpactRecordRepository;
import com.furelise.skillmanagement.repository.SkillCategoryRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillUsageRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final SkillUsageRepository skillUsageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalSkills = skillRepository.countByDeletedAtIsNull();
        
        Map<String, Long> skillsByStatus = new HashMap<>();
        for (String status : List.of("PENDING", "PUBLISHED", "REJECTED", "DEPRECATED", "MERGED")) {
            skillsByStatus.put(status, skillRepository.countByStatusAndDeletedAtIsNull(status));
        }

        ZonedDateTime oneWeekAgo = ZonedDateTime.now().minusDays(7);
        long newThisWeek = skillRepository.countByCreatedAtAfterAndDeletedAtIsNull(oneWeekAgo);

        Long totalUsage = skillRepository.sumTotalUsage();

        ZonedDateTime startOfMonth = ZonedDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal totalMmSavedThisMonth = impactRecordRepository.sumEstimatedMmSavedSince(startOfMonth);

        List<CategoryStat> skillsPerCategory = skillCategoryRepository.findAll().stream()
                .map(cat -> new CategoryStat(
                        cat.getId(), 
                        cat.getName(), 
                        cat.getSkills() != null ? cat.getSkills().size() : 0L))
                .toList();

        return new DashboardStatsResponse(
                totalSkills,
                skillsByStatus,
                newThisWeek,
                totalUsage,
                totalMmSavedThisMonth,
                skillsPerCategory
        );
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard() {
        List<SkillLeaderboardItem> topUsedSkills = skillRepository.findTop10ByStatusAndDeletedAtIsNullOrderByUsageCountDesc("PUBLISHED")
                .stream().map(this::mapToLeaderboardItem).toList();

        List<SkillLeaderboardItem> topRatedSkills = skillRepository.findTop10ByStatusAndDeletedAtIsNullAndRatingCountGreaterThanEqualOrderByAverageRatingDesc("PUBLISHED", 1)
                .stream().map(this::mapToLeaderboardItem).toList();

        List<Object[]> topImpactRaw = impactRecordRepository.findTopImpactSkills();
        List<SkillImpactLeaderboardItem> topImpactSkills = new ArrayList<>();
        int count = 0;
        for (Object[] row : topImpactRaw) {
            if (count >= 10) break;
            Long skillId = (Long) row[0];
            BigDecimal totalMm = (BigDecimal) row[1];
            Long recordCount = (Long) row[2];
            skillRepository.findById(skillId).ifPresent(s -> {
                if ("PUBLISHED".equalsIgnoreCase(s.getStatus()) && s.getDeletedAt() == null) {
                    topImpactSkills.add(new SkillImpactLeaderboardItem(
                            s.getId(),
                            s.getTitle(),
                            getAuthorName(s),
                            totalMm,
                            recordCount
                    ));
                }
            });
            count++;
        }

        List<Object[]> topContributorsRaw = skillRepository.findTopContributors("PUBLISHED");
        List<ContributorLeaderboardItem> topContributors = new ArrayList<>();
        int cCount = 0;
        for (Object[] row : topContributorsRaw) {
            if (cCount >= 10) break;
            Long userId = (Long) row[0];
            Long publishedCount = (Long) row[1];
            Long totalUsageCount = (Long) row[2];
            userRepository.findById(userId).ifPresent(u -> {
                topContributors.add(new ContributorLeaderboardItem(
                        u.getId(),
                        u.getName() != null ? u.getName() : u.getEmail(),
                        u.getAvatarUrl(),
                        publishedCount,
                        totalUsageCount
                ));
            });
            cCount++;
        }

        return new LeaderboardResponse(topUsedSkills, topRatedSkills, topImpactSkills, topContributors);
    }

    @Transactional(readOnly = true)
    public MySkillsStatsResponse getMyStats(User user) {
        long mySkillsCount = skillRepository.countByAuthorIdAndDeletedAtIsNull(user.getId());
        long myPublishedCount = skillRepository.countByAuthorIdAndStatusAndDeletedAtIsNull(user.getId(), "PUBLISHED");
        Long totalUsageOfMySkills = skillRepository.sumUsageByAuthorId(user.getId());
        long skillsUsedByMe = skillUsageRepository.countDistinctSkillsUsedByUserId(user.getId());
        BigDecimal totalMmSavedByMe = impactRecordRepository.sumEstimatedMmSavedByUserId(user.getId());

        return new MySkillsStatsResponse(
                mySkillsCount,
                myPublishedCount,
                totalUsageOfMySkills != null ? totalUsageOfMySkills : 0L,
                skillsUsedByMe,
                totalMmSavedByMe
        );
    }

    private SkillLeaderboardItem mapToLeaderboardItem(Skill skill) {
        return new SkillLeaderboardItem(
                skill.getId(),
                skill.getTitle(),
                getAuthorName(skill),
                skill.getCurrentVersion(),
                skill.getUsageCount() != null ? skill.getUsageCount() : 0L,
                skill.getAverageRating()
        );
    }

    private String getAuthorName(Skill skill) {
        if (skill.getAuthor() == null) return "Unknown";
        return skill.getAuthor().getName() != null ? skill.getAuthor().getName() : skill.getAuthor().getEmail();
    }
}
