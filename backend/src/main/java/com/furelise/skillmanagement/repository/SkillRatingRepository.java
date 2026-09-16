package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRatingRepository extends JpaRepository<SkillRating, Long> {
    Optional<SkillRating> findBySkillIdAndUserId(Long skillId, Long userId);
}
