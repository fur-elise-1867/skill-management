package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.RatingDto.RatingResponse;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillRating;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillRatingRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final SkillRatingRepository skillRatingRepository;
    private final SkillRepository skillRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public RatingResponse rateSkill(Long skillId, User user, Integer ratingValue) {
        Skill skill = skillRepository.findById(skillId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));

        SkillRating rating = skillRatingRepository.findBySkillIdAndUserId(skillId, user.getId())
                .orElseGet(() -> {
                    SkillRating newRating = new SkillRating();
                    newRating.setSkill(skill);
                    newRating.setUser(user);
                    return newRating;
                });

        rating.setRating(ratingValue);
        SkillRating saved = skillRatingRepository.save(rating);

        // Calculate and update average rating & rating count
        Double avg = skillRatingRepository.calculateAverageRating(skillId);
        Long count = skillRatingRepository.countBySkillId(skillId);

        if (avg != null) {
            skill.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        } else {
            skill.setAverageRating(BigDecimal.ZERO);
        }
        skill.setRatingCount(count != null ? count.intValue() : 0);
        skillRepository.save(skill);

        auditLogService.log(user, "SKILL_RATE", "Skill", skillId, "User rated " + ratingValue + " stars");
        log.info("User {} rated skill {} with {} stars. New avg: {}", user.getId(), skillId, ratingValue, skill.getAverageRating());

        return RatingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Optional<RatingResponse> getMyRating(Long skillId, User user) {
        return skillRatingRepository.findBySkillIdAndUserId(skillId, user.getId())
                .map(RatingResponse::from);
    }
}
