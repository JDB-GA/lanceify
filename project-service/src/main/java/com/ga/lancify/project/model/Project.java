package com.ga.lancify.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    private String id;
    private String clientId;
    private String assignedFreelancerId;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDate deadline;
    private String categoryId;

    private ProjectStatus status = ProjectStatus.OPEN;
    private List<String> requiredSkillIds = new ArrayList<>();
    private LocalDateTime createdAt;
}
