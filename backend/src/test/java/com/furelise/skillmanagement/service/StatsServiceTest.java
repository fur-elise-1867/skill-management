package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.StatsDto.DashboardStatsResponse;
import com.furelise.skillmanagement.repository.ImpactRecordRepository;
import com.furelise.skillmanagement.repository.SkillCategoryRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillUsageRepository;
import com.furelise.skillmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillCategoryRepository skillCategoryRepository;

    @Mock
    private ImpactRecordRepository impactRecordRepository;

    @Mock
    private SkillUsageRepository skillUsageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getDashboardStats_shouldReturnStats() {
        when(skillRepository.countByDeletedAtIsNull()).thenReturn(100L);
        when(skillRepository.sumTotalUsage()).thenReturn(500L);
        when(impactRecordRepository.sumEstimatedMmSavedSince(any())).thenReturn(new BigDecimal("10.5"));
        when(skillCategoryRepository.findAll()).thenReturn(List.of());

        DashboardStatsResponse response = statsService.getDashboardStats();

        assertEquals(100L, response.totalSkills());
        assertEquals(500L, response.totalUsage());
        assertEquals(new BigDecimal("10.5"), response.totalMmSavedThisMonth());
    }
}
