package com.ga.lancify.project.model;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Project {
    @Id
    private String id;
    private String clientId;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDate deadline;
//    private List<Skill> requiredSkills;
    private ProjectStatus projectStatus;
    private String assignedFreelancerId;
    private LocalDateTime createdAt;
}
