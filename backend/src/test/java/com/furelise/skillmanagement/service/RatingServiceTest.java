package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.RatingDto.RatingResponse;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillRating;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillRatingRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private SkillRatingRepository skillRatingRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void rateSkill_shouldCreateNewRatingAndCalculateAverage() {
        // Arrange
        Long skillId = 1L;
        User user = new User();
        user.setId(10L);
        user.setName("Test User");

        Skill skill = new Skill();
        skill.setId(skillId);

        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(skillRatingRepository.findBySkillIdAndUserId(skillId, user.getId())).thenReturn(Optional.empty());
        
        SkillRating savedRating = new SkillRating();
        savedRating.setId(100L);
        savedRating.setSkill(skill);
        savedRating.setUser(user);
        savedRating.setRating(5);
        when(skillRatingRepository.save(any(SkillRating.class))).thenReturn(savedRating);

        when(skillRatingRepository.calculateAverageRating(skillId)).thenReturn(5.0);
        when(skillRatingRepository.countBySkillId(skillId)).thenReturn(1L);

        // Act
        RatingResponse response = ratingService.rateSkill(skillId, user, 5);

        // Assert
        assertEquals(5, response.rating());
        verify(skillRepository).save(skill);
        assertEquals(new BigDecimal("5.00"), skill.getAverageRating());
        assertEquals(1, skill.getRatingCount());
        verify(auditLogService).log(eq(user), eq("SKILL_RATE"), eq("Skill"), eq(skillId), anyString());
    }
}
