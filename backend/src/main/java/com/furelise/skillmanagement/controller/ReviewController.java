package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.ReviewDto.ReviewCreateRequest;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewReportRequest;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewResponse;
import com.furelise.skillmanagement.dto.ReviewDto.ReviewUpdateRequest;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // SKILL-RELATED REVIEW ENDPOINTS
    @PostMapping("/skills/{skillId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long skillId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(skillId, user, request));
    }

    @GetMapping("/skills/{skillId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Long skillId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(skillId, pageable));
    }

    // INDIVIDUAL REVIEW ENDPOINTS
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, user, request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Integer> voteHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.voteHelpful(reviewId, user));
    }

    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<Void> reportReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReportRequest request,
            @AuthenticationPrincipal User user) {
        reviewService.reportReview(reviewId, user, request);
        return ResponseEntity.ok().build();
    }
}
