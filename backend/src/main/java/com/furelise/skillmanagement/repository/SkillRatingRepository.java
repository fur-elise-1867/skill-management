package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRatingRepository extends JpaRepository<SkillRating, Long> {
    Optional<SkillRating> findBySkillIdAndUserId(Long skillId, Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM SkillRating r WHERE r.skill.id = :skillId")
    Double calculateAverageRating(@org.springframework.data.repository.query.Param("skillId") Long skillId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM SkillRating r WHERE r.skill.id = :skillId")
    Long countBySkillId(@org.springframework.data.repository.query.Param("skillId") Long skillId);
}
