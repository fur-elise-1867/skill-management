package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.UsageDto.ImpactRecordRequest;
import com.furelise.skillmanagement.dto.UsageDto.ImpactRecordResponse;
import com.furelise.skillmanagement.dto.UsageDto.UsageRecordResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.UsageImpactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills/{skillId}")
@RequiredArgsConstructor
public class UsageController {

    private final UsageImpactService usageImpactService;

    @PostMapping("/usage")
    public ResponseEntity<UsageRecordResponse> recordUsage(
            @PathVariable Long skillId,
            @AuthenticationPrincipal User user) {
        UsageRecordResponse response = usageImpactService.recordUsage(skillId, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/impact")
    public ResponseEntity<ImpactRecordResponse> recordImpact(
            @PathVariable Long skillId,
            @Valid @RequestBody ImpactRecordRequest request,
            @AuthenticationPrincipal User user) {
        ImpactRecordResponse response = usageImpactService.recordImpact(skillId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/impact")
    public ResponseEntity<List<ImpactRecordResponse>> getSkillImpacts(
            @PathVariable Long skillId) {
        return ResponseEntity.ok(usageImpactService.getSkillImpacts(skillId));
    }
}
