package com.ga.lancify.project.controller;

import com.ga.lancify.project.dto.ProjectRequest;
import com.ga.lancify.project.dto.ProjectResponse;
import com.ga.lancify.project.dto.ProjectStatusRequest;
import com.ga.lancify.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // Client posts a new project
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request,
                                                  Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(request, getUserId(auth)));
    }


    // Browse open projects, optionally filtered by ?categoryId= or ?skillIds=a,b
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getOpen(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) List<String> skillIds) {
        return ResponseEntity.ok(projectService.getOpenProjects(categoryId, skillIds));
    }

    // Client's own projects
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMine(Authentication auth) {
        return ResponseEntity.ok(projectService.getMyProjects(getUserId(auth)));
    }


    // Projects the freelancer was hired for
    @PreAuthorize("hasRole('FREELANCER')")
    @GetMapping("/assigned")
    public ResponseEntity<List<ProjectResponse>> getAssigned(Authentication auth) {
        return ResponseEntity.ok(projectService.getAssignedProjects(getUserId(auth)));
    }

    // Project details
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    // Owner edits the project (only while OPEN)
    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable String id,
                                                  @Valid @RequestBody ProjectRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(projectService.update(id, request, getUserId(auth)));
    }


    // Owner changes the status (cancel / complete)
    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> changeStatus(@PathVariable String id,
                                                        @Valid @RequestBody ProjectStatusRequest request,
                                                        Authentication auth) {
        return ResponseEntity.ok(projectService.changeStatus(id, request.getStatus(), getUserId(auth)));
    }

    // Owner hires a freelancer (locked, so only one can be assigned)
    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/{id}/assign/{freelancerId}")
    public ResponseEntity<ProjectResponse> assign(@PathVariable String id,
                                                  @PathVariable String freelancerId,
                                                  Authentication auth) {
        return ResponseEntity.ok(projectService.assignFreelancer(id, freelancerId, getUserId(auth)));
    }

    // Owner (while OPEN) or admin deletes the project
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        projectService.delete(id, getUserId(auth), isAdmin);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Authentication auth) {
        return (String) auth.getDetails();
    }
}
