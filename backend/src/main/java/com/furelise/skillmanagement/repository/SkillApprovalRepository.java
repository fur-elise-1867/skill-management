package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillApprovalRepository extends JpaRepository<SkillApproval, Long> {
    List<SkillApproval> findBySkillIdOrderByCreatedAtDesc(Long skillId);
}
