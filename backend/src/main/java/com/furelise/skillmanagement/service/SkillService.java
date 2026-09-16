package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.SkillDto.*;
import com.furelise.skillmanagement.exception.BadRequestException;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.*;
import com.furelise.skillmanagement.repository.*;
import com.furelise.skillmanagement.service.ContentSecurityScanService.SecurityScanResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillVersionRepository skillVersionRepository;
    private final SkillCategoryRepository categoryRepository;
    private final SkillTagRepository tagRepository;
    private final SkillApprovalRepository approvalRepository;
    private final SkillFileStorageService fileStorageService;
    private final ContentSecurityScanService securityScanService;
    private final AuditLogService auditLogService;

    @Transactional
    public SkillResponse uploadSkill(User author, SkillUploadRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File mã nguồn của skill là bắt buộc");
        }

        // 1. Quét nội dung bảo mật
        String fileContent = fileStorageService.readContent(file);
        SecurityScanResult scanResult = securityScanService.scan(file.getOriginalFilename(), fileContent);

        // 2. Khởi tạo Skill
        Skill skill = new Skill();
        skill.setTitle(request.title().trim());
        skill.setDescription(request.description().trim());
        skill.setAuthor(author);
        skill.setStatus("PENDING");
        skill.setCurrentVersion(1);

        // Gắn Categories
        attachCategories(skill, request.categoryIds());

        // Gắn Tags
        attachTags(skill, request.tagNames());

        Skill savedSkill = skillRepository.save(skill);

        // 3. Lưu trữ file
        String storedPath = fileStorageService.storeFile(savedSkill.getId(), 1, file);
        savedSkill.setFileName(StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "skill.txt"));
        savedSkill.setFilePath(storedPath);
        skillRepository.save(savedSkill);

        // 4. Lưu SkillVersion 1
        SkillVersion version = new SkillVersion();
        version.setSkill(savedSkill);
        version.setVersion(1);
        version.setFileName(savedSkill.getFileName());
        version.setFilePath(storedPath);
        version.setChangelog(request.changelog() != null ? request.changelog() : "Phiên bản khởi tạo ban đầu");
        version.setCreatedBy(author);
        skillVersionRepository.save(version);

        // 5. Lưu kết quả quét vào SkillApproval
        SkillApproval approval = new SkillApproval();
        approval.setSkill(savedSkill);
        approval.setCurator(author); // tạm gắn người nộp, khi Curator duyệt sẽ cập nhật curator_id
        approval.setDecision("PENDING");
        approval.setSecurityScanFlag(scanResult.hasRisk());
        approval.setSecurityScanDetail(scanResult.detailMessage());
        approvalRepository.save(approval);

        // 6. Ghi Audit Log
        auditLogService.log(author, "SKILL_UPLOAD", "SKILL", savedSkill.getId(),
                "Upload skill mới: " + savedSkill.getTitle() + (scanResult.hasRisk() ? " (CẢNH BÁO AN NINH)" : ""));

        return toSkillResponse(savedSkill, request.primaryCategoryId());
    }

    @Transactional
    public SkillResponse updateSkill(Long id, User currentUser, SkillUpdateRequest request, MultipartFile file) {
        Skill skill = getSkillEntity(id);

        boolean isAuthor = skill.getAuthor().getId().equals(currentUser.getId());
        boolean isCuratorOrAdmin = isCuratorOrAdmin(currentUser);

        if (!isAuthor && !isCuratorOrAdmin) {
            throw new AccessDeniedException("Chỉ tác giả hoặc Curator/Admin mới có quyền chỉnh sửa skill này.");
        }

        if (request.title() != null && !request.title().isBlank()) {
            skill.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            skill.setDescription(request.description().trim());
        }
        if (request.categoryIds() != null) {
            skill.getCategories().clear();
            attachCategories(skill, request.categoryIds());
        }
        if (request.tagNames() != null) {
            attachTags(skill, request.tagNames());
        }

        // Nếu có upload file mới -> tăng version
        if (file != null && !file.isEmpty()) {
            int newVersionNumber = skill.getCurrentVersion() + 1;
            String fileContent = fileStorageService.readContent(file);
            SecurityScanResult scanResult = securityScanService.scan(file.getOriginalFilename(), fileContent);

            String storedPath = fileStorageService.storeFile(skill.getId(), newVersionNumber, file);
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "skill.txt");

            skill.setCurrentVersion(newVersionNumber);
            skill.setFileName(originalFileName);
            skill.setFilePath(storedPath);
            skill.setStatus("PENDING"); // cần duyệt lại khi có file mới

            SkillVersion newVersion = new SkillVersion();
            newVersion.setSkill(skill);
            newVersion.setVersion(newVersionNumber);
            newVersion.setFileName(originalFileName);
            newVersion.setFilePath(storedPath);
            newVersion.setChangelog(request.changelog() != null ? request.changelog() : "Cập nhật phiên bản " + newVersionNumber);
            newVersion.setCreatedBy(currentUser);
            skillVersionRepository.save(newVersion);

            // Ghi nhận cảnh báo an ninh cho version mới
            SkillApproval approval = new SkillApproval();
            approval.setSkill(skill);
            approval.setCurator(currentUser);
            approval.setDecision("PENDING");
            approval.setSecurityScanFlag(scanResult.hasRisk());
            approval.setSecurityScanDetail(scanResult.detailMessage());
            approvalRepository.save(approval);
        }

        Skill updatedSkill = skillRepository.save(skill);
        auditLogService.log(currentUser, "SKILL_UPDATE", "SKILL", updatedSkill.getId(),
                "Cập nhật skill: " + updatedSkill.getTitle() + " (v" + updatedSkill.getCurrentVersion() + ")");

        return toSkillResponse(updatedSkill, request.primaryCategoryId());
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillDetail(Long id, User currentUser) {
        Skill skill = getSkillEntity(id);

        if ("PENDING".equals(skill.getStatus()) || "REJECTED".equals(skill.getStatus())) {
            boolean isAuthor = currentUser != null && skill.getAuthor().getId().equals(currentUser.getId());
            boolean isCurator = currentUser != null && isCuratorOrAdmin(currentUser);
            if (!isAuthor && !isCurator) {
                throw new AccessDeniedException("Skill đang chờ duyệt hoặc bị từ chối, bạn không có quyền xem.");
            }
        }

        return toSkillResponse(skill, null);
    }

    @Transactional(readOnly = true)
    public Page<SkillResponse> getSkills(Pageable pageable, String search, Long categoryId, String status) {
        String effectiveStatus = (status != null && !status.isBlank()) ? status : "PUBLISHED";

        List<Skill> allSkills = skillRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null)
                .filter(s -> effectiveStatus.equalsIgnoreCase("ALL") || s.getStatus().equalsIgnoreCase(effectiveStatus))
                .filter(s -> {
                    if (categoryId == null) return true;
                    return s.getCategories().stream().anyMatch(c -> c.getId().equals(categoryId));
                })
                .filter(s -> {
                    if (search == null || search.isBlank()) return true;
                    String term = search.toLowerCase();
                    boolean matchTitle = s.getTitle().toLowerCase().contains(term);
                    boolean matchDesc = s.getDescription().toLowerCase().contains(term);
                    return matchTitle || matchDesc;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSkills.size());
        List<SkillResponse> pageContent = (start <= allSkills.size())
                ? allSkills.subList(start, end).stream().map(s -> toSkillResponse(s, null)).toList()
                : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, allSkills.size());
    }

    @Transactional
    public void softDeleteSkill(Long id, User currentUser) {
        Skill skill = getSkillEntity(id);

        boolean isAuthor = skill.getAuthor().getId().equals(currentUser.getId());
        boolean isCuratorOrAdmin = isCuratorOrAdmin(currentUser);

        if (!isAuthor && !isCuratorOrAdmin) {
            throw new AccessDeniedException("Chỉ tác giả hoặc Curator/Admin mới có quyền xóa skill.");
        }

        skill.setDeletedAt(ZonedDateTime.now());
        skillRepository.save(skill);

        auditLogService.log(currentUser, "SKILL_DELETE", "SKILL", id, "Soft-delete skill: " + skill.getTitle());
    }

    @Transactional(readOnly = true)
    public DownloadFileInfo getDownloadFile(Long id, Integer versionNumber) {
        Skill skill = getSkillEntity(id);

        String relativePath;
        String fileName;

        if (versionNumber == null || versionNumber.equals(skill.getCurrentVersion())) {
            relativePath = skill.getFilePath();
            fileName = skill.getFileName();
        } else {
            SkillVersion ver = skillVersionRepository.findBySkillIdAndVersion(id, versionNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên bản " + versionNumber + " của skill"));
            relativePath = ver.getFilePath();
            fileName = ver.getFileName();
        }

        if (relativePath == null) {
            throw new ResourceNotFoundException("Skill không có tệp đính kèm để tải về");
        }

        Resource resource = fileStorageService.loadFileAsResource(relativePath);
        return new DownloadFileInfo(resource, fileName);
    }

    public record DownloadFileInfo(Resource resource, String fileName) {}

    @Transactional(readOnly = true)
    public List<SkillVersionResponse> getVersions(Long skillId) {
        getSkillEntity(skillId); // check exists
        return skillVersionRepository.findBySkillIdOrderByVersionDesc(skillId).stream()
                .map(SkillVersionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Skill getSkillEntity(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy skill với ID: " + id));
        if (skill.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Skill đã bị xóa");
        }
        return skill;
    }

    private void attachCategories(Skill skill, List<Long> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long catId : categoryIds) {
                categoryRepository.findById(catId).ifPresent(skill.getCategories()::add);
            }
        }
    }

    private void attachTags(Skill skill, List<String> tagNames) {
        // Tag mapping is managed through skill tags
    }

    private SkillResponse toSkillResponse(Skill skill, Long primaryCategoryId) {
        List<SkillCategoryInfo> categoryInfos = skill.getCategories().stream()
                .map(c -> SkillCategoryInfo.from(c, primaryCategoryId != null && primaryCategoryId.equals(c.getId())))
                .toList();

        return SkillResponse.from(skill, categoryInfos, Collections.emptyList());
    }

    private boolean isCuratorOrAdmin(User user) {
        if (user == null || user.getRole() == null) return false;
        String roleName = user.getRole().getName().toUpperCase();
        return "ADMIN".equals(roleName) || "EDITOR".equals(roleName) || "CURATOR".equals(roleName);
    }
}
