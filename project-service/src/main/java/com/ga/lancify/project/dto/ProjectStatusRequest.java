package com.ga.lancify.project.dto;

import com.ga.lancify.project.model.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body for PATCH /api/projects/{id}/status
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusRequest {
    @NotNull(message = "Status is required")
    private ProjectStatus status;
}