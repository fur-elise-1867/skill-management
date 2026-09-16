package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ApprovalDto.MergeSkillsRequest;
import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillApproval;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillApprovalRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillApprovalRepository approvalRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ApprovalService approvalService;

    private User curator;
    private Skill pendingSkill;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().name("EDITOR").build();
        curator = User.builder().id(2L).email("curator@example.com").role(role).build();

        pendingSkill = new Skill();
        pendingSkill.setId(100L);
        pendingSkill.setTitle("AI Code Helper");
        pendingSkill.setStatus("PENDING");
    }

    @Test
    @DisplayName("Curator should approve pending skill")
    void shouldApproveSkill() {
        when(skillRepository.findById(100L)).thenReturn(Optional.of(pendingSkill));

        approvalService.approveSkill(100L, curator);

        assertThat(pendingSkill.getStatus()).isEqualTo("PUBLISHED");
        verify(skillRepository).save(pendingSkill);
        verify(approvalRepository).save(any(SkillApproval.class));
        verify(auditLogService).log(eq(curator), eq("SKILL_APPROVE"), eq("SKILL"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Curator should reject pending skill with reason")
    void shouldRejectSkill() {
        when(skillRepository.findById(100L)).thenReturn(Optional.of(pendingSkill));

        approvalService.rejectSkill(100L, curator, "Thiếu tài liệu hướng dẫn");

        assertThat(pendingSkill.getStatus()).isEqualTo("REJECTED");
        verify(skillRepository).save(pendingSkill);
        verify(approvalRepository).save(any(SkillApproval.class));
        verify(auditLogService).log(eq(curator), eq("SKILL_REJECT"), eq("SKILL"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Curator should merge duplicate skills and recalculate weighted average rating")
    void shouldMergeSkillsWithWeightedRating() {
        Skill source = new Skill();
        source.setId(10L);
        source.setTitle("Source Skill");
        source.setStatus("PUBLISHED");
        source.setAverageRating(new BigDecimal("4.00"));
        source.setRatingCount(2); // total 8.0
        source.setUsageCount(10);

        Skill target = new Skill();
        target.setId(20L);
        target.setTitle("Target Skill");
        target.setStatus("PUBLISHED");
        target.setAverageRating(new BigDecimal("5.00"));
        target.setRatingCount(2); // total 10.0
        target.setUsageCount(15);

        when(skillRepository.findById(10L)).thenReturn(Optional.of(source));
        when(skillRepository.findById(20L)).thenReturn(Optional.of(target));

        MergeSkillsRequest request = new MergeSkillsRequest(10L, 20L, "Trùng lặp tính năng review");
        approvalService.mergeSkills(curator, request);

        // source status -> MERGED, merged_into_id -> 20
        assertThat(source.getStatus()).isEqualTo("MERGED");
        assertThat(source.getMergedInto()).isEqualTo(target);

        // target new rating count -> 4, new average -> (8 + 10) / 4 = 4.50
        assertThat(target.getRatingCount()).isEqualTo(4);
        assertThat(target.getAverageRating()).isEqualTo(new BigDecimal("4.50"));
        // target usage count -> 10 + 15 = 25
        assertThat(target.getUsageCount()).isEqualTo(25);

        verify(skillRepository).save(source);
        verify(skillRepository).save(target);
        verify(auditLogService).log(eq(curator), eq("SKILL_MERGE"), eq("SKILL"), eq(20L), anyString());
    }
}
