package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.dto.UsageDto.ImpactRecordRequest;
import com.furelise.skillmanagement.dto.UsageDto.ImpactRecordResponse;
import com.furelise.skillmanagement.dto.UsageDto.UsageRecordResponse;
import com.furelise.skillmanagement.exception.ResourceNotFoundException;
import com.furelise.skillmanagement.model.ImpactRecord;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillUsage;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.ImpactRecordRepository;
import com.furelise.skillmanagement.repository.SkillRepository;
import com.furelise.skillmanagement.repository.SkillUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageImpactService {

    private final SkillUsageRepository skillUsageRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final SkillRepository skillRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public UsageRecordResponse recordUsage(Long skillId, User user) {
        Skill skill = skillRepository.findById(skillId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Optional<SkillUsage> existingUsage = skillUsageRepository.findBySkillIdAndUserIdAndUsageDate(skillId, user.getId(), today);

        if (existingUsage.isPresent()) {
            // Already recorded today, idempotent success
            long totalUsageCount = skill.getUsageCount() != null ? skill.getUsageCount() : 0L;
            return new UsageRecordResponse(skillId, false, totalUsageCount, "Usage already recorded for today");
        }

        SkillUsage newUsage = new SkillUsage();
        newUsage.setSkill(skill);
        newUsage.setUser(user);
        skillUsageRepository.save(newUsage);

        int currentCount = skill.getUsageCount() != null ? skill.getUsageCount() : 0;
        skill.setUsageCount(currentCount + 1);
        skillRepository.save(skill);

        log.info("User {} recorded usage for skill {} on {}", user.getId(), skillId, today);

        return new UsageRecordResponse(skillId, true, (long) skill.getUsageCount(), "Usage recorded successfully");
    }

    @Transactional
    public ImpactRecordResponse recordImpact(Long skillId, User user, ImpactRecordRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));

        ImpactRecord record = new ImpactRecord();
        record.setSkill(skill);
        record.setUser(user);
        record.setReferenceCode(request.referenceCode());
        record.setEffectivenessScore(request.effectivenessScore());
        record.setEstimatedMmSaved(request.estimatedMmSaved());
        record.setNote(request.note());

        ImpactRecord saved = impactRecordRepository.save(record);
        
        String logDetails = String.format("Recorded impact for reference %s with score %d", request.referenceCode(), request.effectivenessScore());
        auditLogService.log(user, "IMPACT_RECORD", "Skill", skillId, logDetails);
        log.info("User {} recorded impact for skill {} with reference {}", user.getId(), skillId, request.referenceCode());

        return ImpactRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ImpactRecordResponse> getSkillImpacts(Long skillId) {
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill not found with ID: " + skillId);
        }
        return impactRecordRepository.findBySkillId(skillId).stream()
                .map(ImpactRecordResponse::from)
                .toList();
    }
}
