package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillReviewRepository extends JpaRepository<SkillReview, Long> {
    Page<SkillReview> findBySkillId(Long skillId, Pageable pageable);
}
