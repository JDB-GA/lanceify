package com.ga.lanceify.catalog.repository;

import com.ga.lanceify.catalog.model.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends MongoRepository<Skill, String> {
    Optional<Skill> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Skill> findAllByActiveTrue();
    List<Skill> findByIdInAndActiveTrue(List<String> ids);
}
