package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.ImpactRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImpactRecordRepository extends JpaRepository<ImpactRecord, Long> {
    List<ImpactRecord> findBySkillId(Long skillId);
}
