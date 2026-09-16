package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SkillUsageRepository extends JpaRepository<SkillUsage, Long> {
    Optional<SkillUsage> findBySkillIdAndUserIdAndUsageDate(Long skillId, Long userId, LocalDate usageDate);
    long countBySkillId(Long skillId);
}
