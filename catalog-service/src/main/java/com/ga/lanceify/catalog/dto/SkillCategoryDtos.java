package com.ga.lanceify.catalog.dto;

import java.util.List;

public final class SkillCategoryDtos {

    private SkillCategoryDtos() {}

    public record CreateSkillRequest(String name) {}

    public record UpdateSkillRequest(String name, Long version) {}

    public record CreateCategoryRequest(String name, String description, List<String> skillIds) {}

    public record UpdateCategoryRequest(String name, String description, Long version) {}

    public record ReplaceCategorySkillsRequest(List<String> skillIds, Long version) {}

    public record UpdateFreelancerSkillsRequest(List<String> skillIds) {}

    public record SkillResponse(String id, String name, boolean active, Long version) {}

    public record CategoryResponse(
            String id,
            String name,
            String description,
            List<String> skillIds,
            boolean active,
            Long version
    ) {}
}
