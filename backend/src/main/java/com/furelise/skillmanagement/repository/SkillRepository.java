package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(SkillStatus status);

    long countByCreatedAtAfterAndDeletedAtIsNull(ZonedDateTime date);

    @Query("SELECT COALESCE(SUM(s.usageCount), 0) FROM Skill s WHERE s.deletedAt IS NULL")
    Long sumTotalUsage();

    @Query("SELECT COALESCE(SUM(s.usageCount), 0) FROM Skill s WHERE s.author.id = :authorId AND s.deletedAt IS NULL")
    Long sumUsageByAuthorId(@Param("authorId") Long authorId);

    long countByAuthorIdAndDeletedAtIsNull(Long authorId);

    long countByAuthorIdAndStatusAndDeletedAtIsNull(Long authorId, SkillStatus status);

    List<Skill> findTop10ByStatusAndDeletedAtIsNullOrderByUsageCountDesc(SkillStatus status);

    List<Skill> findTop10ByStatusAndDeletedAtIsNullAndRatingCountGreaterThanEqualOrderByAverageRatingDesc(SkillStatus status, Integer minRatings);

    @Query("SELECT s.author.id, COUNT(s) as cnt, COALESCE(SUM(s.usageCount), 0) as totalUsage " +
           "FROM Skill s WHERE s.status = :status AND s.deletedAt IS NULL " +
           "GROUP BY s.author.id ORDER BY cnt DESC, totalUsage DESC")
    List<Object[]> findTopContributors(@Param("status") SkillStatus status);
}
