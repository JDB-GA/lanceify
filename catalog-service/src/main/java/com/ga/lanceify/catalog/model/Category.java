package com.ga.lanceify.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;
    @Indexed(unique = true, collation = "{ 'locale': 'en', 'strength': 2 }")
    private String name;
    private String description;
    private List<String> skillIds = new ArrayList<>();
    private boolean active = true;

    @Version
    private Long version;

    public Category() {}

    public Category(String id, String name, String description, List<String> skillIds, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.skillIds = (skillIds != null) ? skillIds : new ArrayList<>();
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(List<String> skillIds) {
        this.skillIds = skillIds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
