package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ReviewDto.ReviewCreateRequest;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewResponse;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillReview;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private SkillReviewRepository skillReviewRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_shouldSaveReviewAndReturnResponse() {
        // Arrange
        Long skillId = 1L;
        User user = new User();
        user.setId(10L);
        user.setName("Reviewer");

        Skill skill = new Skill();
        skill.setId(skillId);

        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));

        SkillReview savedReview = new SkillReview();
        savedReview.setId(100L);
        savedReview.setSkill(skill);
        savedReview.setUser(user);
        savedReview.setContent("Great skill");
        savedReview.setHelpfulCount(0);

        when(skillReviewRepository.save(any(SkillReview.class))).thenReturn(savedReview);

        ReviewCreateRequest request = new ReviewCreateRequest("Great skill");

        // Act
        ReviewResponse response = reviewService.createReview(skillId, user, request);

        // Assert
        assertEquals(100L, response.id());
        assertEquals("Great skill", response.content());
        verify(auditLogService).log(eq(user), eq("REVIEW_CREATE"), eq("SkillReview"), eq(100L), anyString());
    }
}
