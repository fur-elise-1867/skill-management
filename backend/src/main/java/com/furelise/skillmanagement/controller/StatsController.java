package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.StatsDto.DashboardStatsResponse;
import com.furelise.skillmanagement.dto.StatsDto.LeaderboardResponse;
import com.furelise.skillmanagement.dto.StatsDto.MySkillsStatsResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(statsService.getDashboardStats());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(statsService.getLeaderboard());
    }

    @GetMapping("/my")
    public ResponseEntity<MySkillsStatsResponse> getMyStats(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getMyStats(user));
    }
}
