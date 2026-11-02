package com.ga.lanceify.contract.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProjectClient {

	private final RestClient restClient;

	public ProjectClient(@Value("${project.service.url}") String url) {
		this.restClient = RestClient.create(url);
	}

	public void changeStatus(String projectId, String status) {
		restClient.patch()
				.uri("/api/projects/{id}/status", projectId)
				.header(HttpHeaders.AUTHORIZATION, CallerToken.header())
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("status", status))
				.retrieve()
				.toBodilessEntity();
	}

}
