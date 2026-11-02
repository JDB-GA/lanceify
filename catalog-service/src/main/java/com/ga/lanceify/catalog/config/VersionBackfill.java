package com.ga.lanceify.catalog.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.ga.lanceify.catalog.model.Category;
import com.ga.lanceify.catalog.model.Skill;

@Component
public class VersionBackfill implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    public VersionBackfill(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Query missing = Query.query(Criteria.where("version").exists(false));
        Update initial = Update.update("version", 0L);
        mongoTemplate.updateMulti(missing, initial, Skill.class);
        mongoTemplate.updateMulti(missing, initial, Category.class);
    }

}
