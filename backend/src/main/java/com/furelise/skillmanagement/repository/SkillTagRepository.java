package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.SkillTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillTagRepository extends JpaRepository<SkillTag, Long> {
    Optional<SkillTag> findByName(String name);
}
