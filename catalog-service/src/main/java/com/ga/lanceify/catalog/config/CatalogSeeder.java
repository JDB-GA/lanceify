package com.ga.lanceify.catalog.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ga.lanceify.catalog.model.Category;
import com.ga.lanceify.catalog.model.Skill;
import com.ga.lanceify.catalog.repository.CategoryRepository;
import com.ga.lanceify.catalog.repository.SkillRepository;

@Component
public class CatalogSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

	private static final Map<String, List<String>> CATALOG = new LinkedHashMap<>();

	static {
		CATALOG.put("Web Development", List.of("Java", "Spring Boot", "JavaScript", "TypeScript", "React", "Angular", "Node.js", "HTML", "CSS"));
		CATALOG.put("Mobile Development", List.of("Flutter", "React Native", "Swift", "Kotlin", "Java"));
		CATALOG.put("Data and AI", List.of("Python", "SQL", "Machine Learning", "Data Analysis", "Power BI"));
		CATALOG.put("DevOps and Cloud", List.of("Docker", "Kubernetes", "AWS", "Azure", "CI/CD", "Linux"));
		CATALOG.put("Design", List.of("UI Design", "UX Research", "Figma", "Adobe Photoshop", "Adobe Illustrator"));
		CATALOG.put("Writing and Translation", List.of("Content Writing", "Copywriting", "Technical Writing", "Arabic Translation"));
		CATALOG.put("Marketing", List.of("SEO", "Social Media Marketing", "Email Marketing", "Google Ads"));
	}

	private final SkillRepository skillRepository;

	private final CategoryRepository categoryRepository;

	public CatalogSeeder(SkillRepository skillRepository, CategoryRepository categoryRepository) {
		this.skillRepository = skillRepository;
		this.categoryRepository = categoryRepository;
	}

	@Override
	public void run(String... args) {
		CATALOG.forEach((categoryName, skillNames) -> {
			if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
				return;
			}
			List<String> skillIds = new ArrayList<>();
			for (String skillName : skillNames) {
				skillIds.add(findOrCreateSkill(skillName).getId());
			}
			categoryRepository.save(new Category(UUID.randomUUID().toString(), categoryName, categoryName, skillIds, true));
		});
		log.info("Catalog ready: {} skills, {} categories", skillRepository.count(), categoryRepository.count());
	}

	private Skill findOrCreateSkill(String name) {
		return skillRepository.findByNameIgnoreCase(name)
				.orElseGet(() -> skillRepository.save(new Skill(UUID.randomUUID().toString(), name, true)));
	}

}
