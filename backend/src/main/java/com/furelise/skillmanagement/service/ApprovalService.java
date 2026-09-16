package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.ApprovalDto.*;
import com.furelise.skillmanagement.dto.SkillDto.AuthorInfo;
import com.furelise.skillmanagement.dto.SkillDto.SkillCategoryInfo;
import com.furelise.skillmanagement.exception.BadRequestException;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillApproval;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.SkillApprovalRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final SkillRepository skillRepository;
    private final SkillApprovalRepository approvalRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PendingSkillResponse> getPendingSkills(Pageable pageable) {
        List<Skill> pendingList = skillRepository.findAll().stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()) && s.getDeletedAt() == null)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), pendingList.size());
        List<PendingSkillResponse> pageContent = (start <= pendingList.size())
                ? pendingList.subList(start, end).stream().map(this::toPendingSkillResponse).toList()
                : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, pendingList.size());
    }

    @Transactional
    public void approveSkill(Long skillId, User curator) {
        Skill skill = findSkill(skillId);
        if (!"PENDING".equalsIgnoreCase(skill.getStatus())) {
            throw new BadRequestException("Skill không ở trạng thái chờ duyệt (PENDING)");
        }

        skill.setStatus("PUBLISHED");
        skillRepository.save(skill);

        SkillApproval approval = new SkillApproval();
        approval.setSkill(skill);
        approval.setCurator(curator);
        approval.setDecision("APPROVED");
        approvalRepository.save(approval);

        auditLogService.log(curator, "SKILL_APPROVE", "SKILL", skillId,
                "Phê duyệt skill: " + skill.getTitle());
    }

    @Transactional
    public void rejectSkill(Long skillId, User curator, String reason) {
        Skill skill = findSkill(skillId);
        if (!"PENDING".equalsIgnoreCase(skill.getStatus())) {
            throw new BadRequestException("Skill không ở trạng thái chờ duyệt (PENDING)");
        }

        skill.setStatus("REJECTED");
        skillRepository.save(skill);

        SkillApproval approval = new SkillApproval();
        approval.setSkill(skill);
        approval.setCurator(curator);
        approval.setDecision("REJECTED");
        approval.setReason(reason);
        approvalRepository.save(approval);

        auditLogService.log(curator, "SKILL_REJECT", "SKILL", skillId,
                "Từ chối skill: " + skill.getTitle() + " - Lý do: " + reason);
    }

    @Transactional
    public void deprecateSkill(Long skillId, User curator, String reason) {
        Skill skill = findSkill(skillId);

        skill.setStatus("DEPRECATED");
        skill.setDeprecatedReason(reason);
        skillRepository.save(skill);

        auditLogService.log(curator, "SKILL_DEPRECATE", "SKILL", skillId,
                "Đánh dấu DEPRECATED cho skill: " + skill.getTitle() + " - Lý do: " + reason);
    }

    @Transactional
    public void mergeSkills(User curator, MergeSkillsRequest request) {
        if (request.sourceSkillId().equals(request.targetSkillId())) {
            throw new BadRequestException("Không thể gộp skill vào chính nó");
        }

        Skill source = findSkill(request.sourceSkillId());
        Skill target = findSkill(request.targetSkillId());

        if ("MERGED".equalsIgnoreCase(source.getStatus())) {
            throw new BadRequestException("Skill nguồn đã bị gộp trước đó");
        }

        // Cập nhật trạng thái skill nguồn
        source.setStatus("MERGED");
        source.setMergedInto(target);

        // Tính lại rating trung bình có trọng số cho skill đích
        int countA = target.getRatingCount() != null ? target.getRatingCount() : 0;
        BigDecimal avgA = target.getAverageRating() != null ? target.getAverageRating() : BigDecimal.ZERO;

        int countB = source.getRatingCount() != null ? source.getRatingCount() : 0;
        BigDecimal avgB = source.getAverageRating() != null ? source.getAverageRating() : BigDecimal.ZERO;

        int totalCount = countA + countB;
        if (totalCount > 0) {
            BigDecimal totalScore = avgA.multiply(BigDecimal.valueOf(countA))
                    .add(avgB.multiply(BigDecimal.valueOf(countB)));
            BigDecimal newAvg = totalScore.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            target.setAverageRating(newAvg);
            target.setRatingCount(totalCount);
        }

        // Gộp lượt dùng
        int usageA = target.getUsageCount() != null ? target.getUsageCount() : 0;
        int usageB = source.getUsageCount() != null ? source.getUsageCount() : 0;
        target.setUsageCount(usageA + usageB);

        skillRepository.save(source);
        skillRepository.save(target);

        auditLogService.log(curator, "SKILL_MERGE", "SKILL", target.getId(),
                String.format("Gộp skill #%d (%s) vào skill #%d (%s) - Lý do: %s",
                        source.getId(), source.getTitle(), target.getId(), target.getTitle(), request.reason()));
    }

    private Skill findSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy skill với ID: " + id));
        if (skill.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Skill đã bị xóa");
        }
        return skill;
    }

    private PendingSkillResponse toPendingSkillResponse(Skill skill) {
        List<SkillApproval> approvals = approvalRepository.findBySkillIdOrderByCreatedAtDesc(skill.getId());
        boolean hasRisk = false;
        String riskDetail = null;
        if (!approvals.isEmpty()) {
            hasRisk = approvals.get(0).getSecurityScanFlag();
            riskDetail = approvals.get(0).getSecurityScanDetail();
        }

        List<SkillCategoryInfo> catInfos = skill.getCategories().stream()
                .map(c -> SkillCategoryInfo.from(c, false))
                .toList();

        return new PendingSkillResponse(
                skill.getId(),
                skill.getTitle(),
                skill.getDescription(),
                AuthorInfo.from(skill.getAuthor()),
                skill.getCurrentVersion(),
                skill.getFileName(),
                catInfos,
                Collections.emptyList(),
                hasRisk,
                riskDetail,
                skill.getCreatedAt()
        );
    }
}
