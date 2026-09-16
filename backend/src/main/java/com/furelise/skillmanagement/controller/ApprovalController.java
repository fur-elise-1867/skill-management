package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.ApprovalDto.*;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.ApprovalService;
import com.furelise.skillmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserService userService;

    @GetMapping("/pending")
    public ResponseEntity<Page<PendingSkillResponse>> getPendingSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(approvalService.getPendingSkills(pageable));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveSkill(@PathVariable Long id) {
        User curator = userService.getAuthenticatedUser();
        approvalService.approveSkill(id, curator);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt skill thành công."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectSkill(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request
    ) {
        User curator = userService.getAuthenticatedUser();
        approvalService.rejectSkill(id, curator, request.reason());
        return ResponseEntity.ok(Map.of("message", "Đã từ chối skill."));
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, String>> mergeSkills(
            @Valid @RequestBody MergeSkillsRequest request
    ) {
        User curator = userService.getAuthenticatedUser();
        approvalService.mergeSkills(curator, request);
        return ResponseEntity.ok(Map.of("message", "Đã gộp skill thành công."));
    }
}
