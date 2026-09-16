package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.UsageDto.UsageRecordResponse;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.ImpactRecordRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageImpactServiceTest {

    @Mock
    private SkillUsageRepository skillUsageRepository;

    @Mock
    private ImpactRecordRepository impactRecordRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UsageImpactService usageImpactService;

    @Test
    void recordUsage_shouldCreateUsageIfFirstTimeToday() {
        Long skillId = 1L;
        User user = new User();
        user.setId(10L);
        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setUsageCount(5);

        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(skillUsageRepository.findBySkillIdAndUserIdAndUsageDate(eq(skillId), eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        UsageRecordResponse response = usageImpactService.recordUsage(skillId, user);

        assertTrue(response.newlyRecorded());
        verify(skillUsageRepository).save(any());
        verify(skillRepository).save(skill);
        assertTrue(skill.getUsageCount() == 6);
    }
}
