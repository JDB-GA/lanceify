package com.ga.lanceify.catalog.dto;

import java.util.List;

public final class SkillCategoryDtos {

    private SkillCategoryDtos() {}

    // Request Records
    public record CreateSkillRequest(String name) {}

    public record UpdateSkillRequest(String name) {}

    public record CreateCategoryRequest(String name, String description, List<String> skillIds) {}

    public record UpdateCategoryRequest(String name, String description) {}

    public record ReplaceCategorySkillsRequest(List<String> skillIds) {}

    public record UpdateFreelancerSkillsRequest(List<String> skillIds) {}

    // Response Records
    public record SkillResponse(String id, String name, boolean active) {}

    public record CategoryResponse(
            String id,
            String name,
            String description,
            List<String> skillIds,
            boolean active
    ) {}
}
