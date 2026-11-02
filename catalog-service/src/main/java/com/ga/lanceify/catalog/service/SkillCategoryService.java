package com.ga.lanceify.catalog.service;

import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CategoryResponse;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CreateCategoryRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CreateSkillRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.ReplaceCategorySkillsRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.SkillResponse;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.UpdateCategoryRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.UpdateSkillRequest;
import com.ga.lanceify.catalog.model.Category;
import com.ga.lanceify.catalog.model.Skill;
import com.ga.lanceify.catalog.repository.CategoryRepository;
import com.ga.lanceify.catalog.repository.SkillRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SkillCategoryService {

    private final SkillRepository skillRepository;
    private final CategoryRepository categoryRepository;

    public SkillCategoryService(SkillRepository skillRepository, CategoryRepository categoryRepository) {
        this.skillRepository = skillRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<SkillResponse> getActiveSkills(String categoryId) {
        List<Skill> skills;
        if (categoryId != null && !categoryId.isBlank()) {
            Category category = categoryRepository.findById(categoryId)
                    .filter(Category::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            skills = skillRepository.findByIdInAndActiveTrue(category.getSkillIds());
        } else {
            skills = skillRepository.findAllByActiveTrue();
        }
        return skills.stream().map(this::toSkillResponse).toList();
    }

    public SkillResponse createSkill(CreateSkillRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill name is required");
        }
        String cleanName = req.name().trim();
        if (skillRepository.existsByNameIgnoreCase(cleanName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Skill with this name already exists");
        }

        Skill skill = new Skill(UUID.randomUUID().toString(), cleanName, true);
        Skill saved = skillRepository.save(skill);
        return toSkillResponse(saved);
    }

    public SkillResponse updateSkill(String id, UpdateSkillRequest req) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        if (req.name() == null || req.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill name is required");
        }
        requireVersion(skill.getVersion(), req.version());
        String cleanName = req.name().trim();
        if (!skill.getName().equalsIgnoreCase(cleanName) && skillRepository.existsByNameIgnoreCase(cleanName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Skill with this name already exists");
        }

        skill.setName(cleanName);
        Skill updated = skillRepository.save(skill);
        return toSkillResponse(updated);
    }

    public void deactivateSkill(String id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));
        skill.setActive(false);
        skillRepository.save(skill);
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrue()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryResponse createCategory(CreateCategoryRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        String cleanName = req.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists");
        }

        List<String> assignedSkillIds = new ArrayList<>();
        if (req.skillIds() != null && !req.skillIds().isEmpty()) {
            List<Skill> validSkills = skillRepository.findByIdInAndActiveTrue(req.skillIds());
            if (validSkills.size() != req.skillIds().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more skills do not exist or are inactive");
            }
            assignedSkillIds.addAll(req.skillIds());
        }

        Category category = new Category(
                UUID.randomUUID().toString(),
                cleanName,
                req.description(),
                assignedSkillIds,
                true
        );

        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    public CategoryResponse updateCategory(String id, UpdateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (req.name() == null || req.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        requireVersion(category.getVersion(), req.version());
        String cleanName = req.name().trim();
        if (!category.getName().equalsIgnoreCase(cleanName) && categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists");
        }

        category.setName(cleanName);
        category.setDescription(req.description());
        Category updated = categoryRepository.save(category);
        return toCategoryResponse(updated);
    }

    public void deactivateCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    public CategoryResponse replaceCategorySkills(String id, ReplaceCategorySkillsRequest req) {
        Category category = categoryRepository.findById(id)
                .filter(Category::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active category not found"));

        if (req.skillIds() == null || req.skillIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one skill ID is required");
        }
        requireVersion(category.getVersion(), req.version());

        List<Skill> validSkills = skillRepository.findByIdInAndActiveTrue(req.skillIds());
        if (validSkills.size() != req.skillIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more skills do not exist or are inactive");
        }

        category.setSkillIds(req.skillIds());
        Category updated = categoryRepository.save(category);
        return toCategoryResponse(updated);
    }

    private void requireVersion(Long current, Long expected) {
        if (expected != null && !expected.equals(current)) {
            throw new OptimisticLockingFailureException("Stale version");
        }
    }

    private SkillResponse toSkillResponse(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.isActive(), skill.getVersion());
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSkillIds(),
                category.isActive(),
                category.getVersion()
        );
    }
}
