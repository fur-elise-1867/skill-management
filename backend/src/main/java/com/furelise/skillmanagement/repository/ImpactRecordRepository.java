package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.ImpactRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ImpactRecordRepository extends JpaRepository<ImpactRecord, Long> {

    List<ImpactRecord> findBySkillId(Long skillId);

    @Query("SELECT COALESCE(SUM(i.estimatedMmSaved), 0) FROM ImpactRecord i WHERE i.createdAt >= :startDate")
    BigDecimal sumEstimatedMmSavedSince(@Param("startDate") ZonedDateTime startDate);

    @Query("SELECT COALESCE(SUM(i.estimatedMmSaved), 0) FROM ImpactRecord i WHERE i.user.id = :userId")
    BigDecimal sumEstimatedMmSavedByUserId(@Param("userId") Long userId);

    @Query("SELECT i.skill.id, SUM(i.estimatedMmSaved) as totalMm, COUNT(i) as cnt FROM ImpactRecord i GROUP BY i.skill.id ORDER BY totalMm DESC")
    List<Object[]> findTopImpactSkills();
}
