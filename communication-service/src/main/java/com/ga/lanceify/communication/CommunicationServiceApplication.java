package com.ga.lanceify.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CommunicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicationServiceApplication.class, args);
	}

}
