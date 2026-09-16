package com.furelise.skillmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "skill_usage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"skill_id", "user_id", "usage_date"})
})
@Getter
@Setter
public class SkillUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "used_at", nullable = false, updatable = false)
    private ZonedDateTime usedAt = ZonedDateTime.now();

    @Column(name = "usage_date", insertable = false, updatable = false)
    private LocalDate usageDate;
}
