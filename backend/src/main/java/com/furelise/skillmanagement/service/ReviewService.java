package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ReviewDto.ReviewCreateRequest;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewReportRequest;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewResponse;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewUpdateRequest;
import com.furelise.skillmanagement.exception.BadRequestException;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillReview;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final SkillReviewRepository skillReviewRepository;
    private final SkillRepository skillRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ReviewResponse createReview(Long skillId, User user, ReviewCreateRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));

        SkillReview review = new SkillReview();
        review.setSkill(skill);
        review.setUser(user);
        review.setContent(request.content());
        review.setHelpfulCount(0);

        SkillReview saved = skillReviewRepository.save(review);
        auditLogService.log(user, "REVIEW_CREATE", "SkillReview", saved.getId(), "Created review for skill " + skillId);
        log.info("User {} created review {} for skill {}", user.getId(), saved.getId(), skillId);

        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(Long skillId, Pageable pageable) {
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill not found with ID: " + skillId);
        }
        return skillReviewRepository.findBySkillId(skillId, pageable)
                .map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, User user, ReviewUpdateRequest request) {
        SkillReview review = skillReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUser().getId().equals(user.getId()) && !isAdminOrEditor(user)) {
            throw new BadRequestException("You do not have permission to edit this review");
        }

        review.setContent(request.content());
        SkillReview updated = skillReviewRepository.save(review);
        
        auditLogService.log(user, "REVIEW_UPDATE", "SkillReview", reviewId, "Updated review content");
        return ReviewResponse.from(updated);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        SkillReview review = skillReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUser().getId().equals(user.getId()) && !isAdminOrEditor(user)) {
            throw new BadRequestException("You do not have permission to delete this review");
        }

        skillReviewRepository.delete(review);
        auditLogService.log(user, "REVIEW_DELETE", "SkillReview", reviewId, "Deleted review");
        log.info("User {} deleted review {}", user.getId(), reviewId);
    }

    @Transactional
    public Integer voteHelpful(Long reviewId, User user) {
        SkillReview review = skillReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // Note: In a production scenario, we'd want to track which user voted to prevent multiple votes from the same user.
        // For Phase 3 scope as per SAS, we just increment the counter.
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        skillReviewRepository.save(review);
        return review.getHelpfulCount();
    }

    @Transactional
    public void reportReview(Long reviewId, User user, ReviewReportRequest request) {
        SkillReview review = skillReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        review.setReportCategory(request.category());
        skillReviewRepository.save(review);

        String details = "Reported as " + request.category() + ". Detail: " + request.detail();
        auditLogService.log(user, "REVIEW_REPORT", "SkillReview", reviewId, details);
        log.info("User {} reported review {} as {}", user.getId(), reviewId, request.category());
    }

    private boolean isAdminOrEditor(User user) {
        String role = user.getRole() != null ? user.getRole().getName() : "";
        return "ADMIN".equals(role) || "EDITOR".equals(role);
    }
}
