package com.ga.lanceify.identity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Fills @CreatedDate and @LastModifiedDate on User.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
