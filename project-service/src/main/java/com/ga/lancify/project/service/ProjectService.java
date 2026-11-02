package com.ga.lancify.project.service;

import com.ga.lancify.project.client.CatalogClient;
import com.ga.lancify.project.dto.ProjectRequest;
import com.ga.lancify.project.dto.ProjectResponse;
import com.ga.lancify.project.exception.InvalidStatusException;
import com.ga.lancify.project.exception.ProjectNotFoundException;
import com.ga.lancify.project.exception.UnauthorizedActionException;
import com.ga.lancify.project.model.Project;
import com.ga.lancify.project.model.ProjectStatus;
import com.ga.lancify.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final CatalogClient catalogClient;

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ProjectResponse create(ProjectRequest request, String clientId) {
        validateSkills(request);

        Project project = new Project();
        project.setClientId(clientId);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setBudget(request.getBudget());
        project.setDeadline(request.getDeadline());
        project.setCategoryId(request.getCategoryId());
        project.setRequiredSkillIds(request.getRequiredSkillIds());
        project.setStatus(ProjectStatus.OPEN);
        project.setCreatedAt(LocalDateTime.now());

        return toResponse(projectRepository.save(project));
    }


    private ProjectResponse toResponse(Project p) {
        ProjectResponse response = new ProjectResponse();
        response.setId(p.getId());
        response.setClientId(p.getClientId());
        response.setAssignedFreelancerId(p.getAssignedFreelancerId());
        response.setTitle(p.getTitle());
        response.setDescription(p.getDescription());
        response.setBudget(p.getBudget());
        response.setDeadline(p.getDeadline());
        response.setCategoryId(p.getCategoryId());
        response.setRequiredSkillIds(p.getRequiredSkillIds());
        response.setStatus(p.getStatus());
        response.setCreatedAt(p.getCreatedAt());
        return response;
    }

    public List<ProjectResponse> getOpenProjects(String categoryId, List<String> skillIds) {
        List<Project> projects;
        if (categoryId != null && !categoryId.isBlank()) {
            projects = projectRepository.findByStatusAndCategoryId(ProjectStatus.OPEN, categoryId);
        } else if (skillIds != null && !skillIds.isEmpty()) {
            projects = projectRepository.findByStatusAndRequiredSkillIdsIn(ProjectStatus.OPEN, skillIds);
        } else {
            projects = projectRepository.findByStatus(ProjectStatus.OPEN);
        }
        return projects.stream().map(this::toResponse).toList();
    }


    public ProjectResponse update(String id, ProjectRequest request, String clientId) {
        Project project = findProject(id);
        checkOwner(project, clientId);

        // Can only edit while no freelancer is hired yet
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new InvalidStatusException("Only OPEN projects can be edited");
        }
        validateSkills(request);

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setBudget(request.getBudget());
        project.setDeadline(request.getDeadline());
        project.setCategoryId(request.getCategoryId());
        project.setRequiredSkillIds(request.getRequiredSkillIds());
        return toResponse(projectRepository.save(project));
    }

    public void delete(String id, String userId, boolean isAdmin) {
        Project project = findProject(id);
        if (!isAdmin) {
            checkOwner(project, userId);
            if (project.getStatus() != ProjectStatus.OPEN) {
                throw new InvalidStatusException("Only OPEN projects can be deleted");
            }
        }
        projectRepository.delete(project);
    }

    public ProjectResponse changeStatus(String id, ProjectStatus newStatus, String clientId, boolean admin) {
        Project project = findProject(id);
        if (!admin) {
            checkOwner(project, clientId);
        }

        // Allowed transitions only
        boolean valid = switch (project.getStatus()) {
            case OPEN -> newStatus == ProjectStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ProjectStatus.COMPLETED || newStatus == ProjectStatus.CANCELLED;
            default -> false;
        };
        if (!valid) {
            throw new InvalidStatusException("Cannot change status from " + project.getStatus() + " to " + newStatus);
        }

        project.setStatus(newStatus);
        return toResponse(projectRepository.save(project));
    }

    // Called when the client accepts a proposal; locked so only one freelancer can be assigned
    public ProjectResponse assignFreelancer(String projectId, String freelancerId, String clientId, boolean admin) {
        ReentrantLock lock = locks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();
        try {
            Project project = findProject(projectId);
            if (!admin) {
                checkOwner(project, clientId);
            }

            if (project.getStatus() != ProjectStatus.OPEN) {
                throw new InvalidStatusException("Project is no longer open");
            }

            project.setAssignedFreelancerId(freelancerId);
            project.setStatus(ProjectStatus.IN_PROGRESS);
            return toResponse(projectRepository.save(project));
        } finally {
            lock.unlock();
        }
    }

    public ProjectResponse getById(String id) {
        return toResponse(findProject(id));
    }

    public List<ProjectResponse> getMyProjects(String clientId) {
        return projectRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }


    public List<ProjectResponse> getAssignedProjects(String freelancerId) {
        return projectRepository.findByAssignedFreelancerId(freelancerId).stream().map(this::toResponse).toList();
    }

    private void validateSkills(ProjectRequest request) {
        Set<String> allowed = catalogClient.getSkillIdsForCategory(request.getCategoryId());
        if (!allowed.containsAll(request.getRequiredSkillIds())) {
            throw new InvalidStatusException("Some skills don't belong to the selected category");
        }
    }

    private Project findProject(String id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private void checkOwner(Project project, String clientId) {
        if (!project.getClientId().equals(clientId)) {
            throw new UnauthorizedActionException("You are not the owner of this project");
        }
    }
}
