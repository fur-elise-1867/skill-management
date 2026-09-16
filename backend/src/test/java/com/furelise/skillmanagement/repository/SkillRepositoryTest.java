package com.furelise.skillmanagement.repository;

import com.furelise.skillmanagement.model.Role;
import com.furelise.skillmanagement.model.Skill;
import com.furelise.skillmanagement.model.SkillCategory;
import com.furelise.skillmanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SkillRepositoryTest {

    @Autowired
    private SkillCategoryRepository categoryRepo;

    @Autowired
    private SkillRepository skillRepo;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private RoleRepository roleRepo;

    private User author;

    @BeforeEach
    void setUp() {
        Role role = roleRepo.save(Role.builder()
                .name("USER")
                .description("Standard User")
                .build());

        author = userRepo.save(User.builder()
                .name("Skill Author")
                .email("author@example.com")
                .password("encoded_pass")
                .role(role)
                .enabled(true)
                .build());
    }

    @Test
    @DisplayName("Should save and find skill with category")
    void shouldSaveAndFindSkill() {
        SkillCategory cat = new SkillCategory();
        cat.setName("Development");
        cat.setDescription("Coding and development");
        categoryRepo.save(cat);

        Skill skill = new Skill();
        skill.setTitle("AI Prompt Template");
        skill.setDescription("A prompt template for code review");
        skill.setAuthor(author);
        skill.setStatus("PENDING");
        skill.getCategories().add(cat);
        
        Skill savedSkill = skillRepo.save(skill);

        Optional<Skill> found = skillRepo.findById(savedSkill.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("AI Prompt Template");
        assertThat(found.get().getCategories()).hasSize(1);
    }
}
