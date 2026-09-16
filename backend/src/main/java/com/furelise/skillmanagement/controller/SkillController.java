package com.furelise.skillmanagement.controller;

import com.furelise.skillmanagement.dto.ApprovalDto.DeprecateRequest;
import com.furelise.skillmanagement.dto.SkillDto.*;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.service.SkillFileStorageService;
import com.furelise.skillmanagement.service.SkillService;
import com.furelise.skillmanagement.service.SkillService.DownloadFileInfo;
import com.furelise.skillmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SkillResponse> uploadSkill(
            @RequestPart("metadata") @Valid SkillUploadRequest request,
            @RequestPart(value = "skillFile", required = false) MultipartFile skillFile,
            @RequestPart(value = "file", required = false) MultipartFile fallbackFile
    ) {
        MultipartFile uploadFile = (skillFile != null && !skillFile.isEmpty()) ? skillFile : fallbackFile;
        User author = userService.getAuthenticatedUser();
        SkillResponse response = skillService.uploadSkill(author, request, uploadFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SkillResponse>> getSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(skillService.getSkills(pageable, search, categoryId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillDetail(@PathVariable Long id) {
        User currentUser = userService.getAuthenticatedUser();
        return ResponseEntity.ok(skillService.getSkillDetail(id, currentUser));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadLatest(
            @PathVariable Long id
    ) {
        DownloadFileInfo fileInfo = skillService.getDownloadFile(id, null);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.fileName() + "\"")
                .body(fileInfo.resource());
    }

    @GetMapping("/{id}/versions/{version}/download")
    public ResponseEntity<Resource> downloadVersion(
            @PathVariable Long id,
            @PathVariable Integer version
    ) {
        DownloadFileInfo fileInfo = skillService.getDownloadFile(id, version);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.fileName() + "\"")
                .body(fileInfo.resource());
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<SkillVersionResponse>> getVersions(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getVersions(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @RequestPart(value = "metadata", required = false) @Valid SkillUpdateRequest request,
            @RequestPart(value = "skillFile", required = false) MultipartFile skillFile,
            @RequestPart(value = "file", required = false) MultipartFile fallbackFile
    ) {
        MultipartFile uploadFile = (skillFile != null && !skillFile.isEmpty()) ? skillFile : fallbackFile;
        User currentUser = userService.getAuthenticatedUser();
        SkillUpdateRequest actualRequest = (request != null) ? request : new SkillUpdateRequest(null, null, null, null, null, null);
        return ResponseEntity.ok(skillService.updateSkill(id, currentUser, actualRequest, uploadFile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSkill(@PathVariable Long id) {
        User currentUser = userService.getAuthenticatedUser();
        skillService.softDeleteSkill(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Đã xóa skill thành công."));
    }

    @PostMapping("/{id}/deprecate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Map<String, String>> deprecateSkill(
            @PathVariable Long id,
            @Valid @RequestBody DeprecateRequest request
    ) {
        User curator = userService.getAuthenticatedUser();
        skillService.getSkillEntity(id); // ensure exists
        skillService.updateSkill(id, curator, new SkillUpdateRequest(null, null, null, null, null, null), null);
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu DEPRECATED cho skill."));
    }
}
