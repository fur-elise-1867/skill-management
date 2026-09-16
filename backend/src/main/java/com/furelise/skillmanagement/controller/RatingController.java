package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.RatingDto.RatingRequest;
import com.furelise.skillmanagement.dto.RatingDto.RatingResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/skills/{skillId}/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> rateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ratingService.rateSkill(skillId, user, request.rating()));
    }

    @GetMapping("/my")
    public ResponseEntity<RatingResponse> getMyRating(
            @PathVariable Long skillId,
            @AuthenticationPrincipal User user) {
        return ratingService.getMyRating(skillId, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
