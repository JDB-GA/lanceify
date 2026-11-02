package com.ga.lanceify.proposal.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ga.lanceify.proposal.dto.ProjectSummary;
import com.ga.lanceify.proposal.exception.ResourceNotFoundException;

@Component
public class ProjectClient {

    private final RestClient restClient;

    public ProjectClient(@Value("${project.service.url}") String projectServiceUrl) {
        this.restClient = RestClient.create(projectServiceUrl);
    }

    public ProjectSummary getProject(String projectId) {
        try {
            ProjectSummary project = restClient.get()
                    .uri("/api/projects/{id}", projectId)
                    .header(HttpHeaders.AUTHORIZATION, callerToken())
                    .retrieve()
                    .body(ProjectSummary.class);
            if (project == null) {
                throw new ResourceNotFoundException("Project not found");
            }
            return project;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Project not found");
        }
    }

    private String callerToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "";
        }
        String header = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        return header == null ? "" : header;
    }

}
