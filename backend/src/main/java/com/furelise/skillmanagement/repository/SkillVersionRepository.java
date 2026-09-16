package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillVersionRepository extends JpaRepository<SkillVersion, Long> {
    List<SkillVersion> findBySkillIdOrderByVersionDesc(Long skillId);
    Optional<SkillVersion> findBySkillIdAndVersion(Long skillId, Integer version);
}
