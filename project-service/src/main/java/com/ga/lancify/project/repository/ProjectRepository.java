package com.ga.lancify.project.repository;

import com.ga.lancify.project.model.Project;
import com.ga.lancify.project.model.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByClientId(String clientId);
    List<Project> findByAssignedFreelancerId(String freelancerId);
    List<Project> findByStatusAndCategoryId(ProjectStatus status, String categoryId);
    List<Project> findByStatusAndRequiredSkillIdsIn(ProjectStatus status, List<String> skillIds);
}
