package com.ga.lancify.project.dto;

import com.ga.lancify.project.model.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectResponse {
    private String id;
    private String clientId;
    private String assignedFreelancerId;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDate deadline;
    private List<String> requiredSkillIds;
    private ProjectStatus status;
    private LocalDateTime createdAt;

}
