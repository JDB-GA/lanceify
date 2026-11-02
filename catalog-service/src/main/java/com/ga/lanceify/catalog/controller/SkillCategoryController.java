package com.ga.lanceify.catalog.controller;

import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CategoryResponse;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CreateCategoryRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.CreateSkillRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.ReplaceCategorySkillsRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.SkillResponse;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.UpdateCategoryRequest;
import com.ga.lanceify.catalog.dto.SkillCategoryDtos.UpdateSkillRequest;
import com.ga.lanceify.catalog.service.SkillCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;

    public SkillCategoryController(SkillCategoryService skillCategoryService) {
        this.skillCategoryService = skillCategoryService;
    }

    // ==========================================
    // SKILLS ENDPOINTS
    // ==========================================

    // GET /api/skills or GET /api/skills?categoryId={id}
    @GetMapping("/skills")
    public ResponseEntity<List<SkillResponse>> getActiveSkills(
            @RequestParam(required = false) String categoryId) {
        List<SkillResponse> skills = skillCategoryService.getActiveSkills(categoryId);
        return ResponseEntity.ok(skills);
    }

    // POST /api/skills (Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/skills")
    public ResponseEntity<SkillResponse> createSkill(
            @RequestBody CreateSkillRequest request) {
        SkillResponse createdSkill = skillCategoryService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSkill);
    }

    // PUT /api/skills/{id} (Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/skills/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable String id,
            @RequestBody UpdateSkillRequest request) {
        SkillResponse updatedSkill = skillCategoryService.updateSkill(id, request);
        return ResponseEntity.ok(updatedSkill);
    }

    // DELETE /api/skills/{id} (Admin - Soft Deactivation)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deactivateSkill(@PathVariable String id) {
        skillCategoryService.deactivateSkill(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // CATEGORIES ENDPOINTS
    // ==========================================

    // GET /api/categories
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories = skillCategoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    // POST /api/categories (Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request) {
        CategoryResponse createdCategory = skillCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    // PUT /api/categories/{id} (Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request) {
        CategoryResponse updatedCategory = skillCategoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    // DELETE /api/categories/{id} (Admin - Soft Deactivation)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deactivateCategory(@PathVariable String id) {
        skillCategoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/categories/{id}/skills (Admin - Replace Skills Assigned to Category)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{id}/skills")
    public ResponseEntity<CategoryResponse> replaceCategorySkills(
            @PathVariable String id,
            @RequestBody ReplaceCategorySkillsRequest request) {
        CategoryResponse updatedCategory = skillCategoryService.replaceCategorySkills(id, request);
        return ResponseEntity.ok(updatedCategory);
    }
}
