package com.ga.lancify.project.client;

import com.ga.lancify.project.exception.InvalidStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CatalogClient {
    private final RestClient restClient;

    public CatalogClient(@Value("${catalog.service.url}") String catalogUrl) {
        this.restClient = RestClient.create(catalogUrl);
    }

    // Local copy of catalog's SkillResponse
    public record SkillResponse(String id, String name, boolean active) {}

    // Returns the active skill IDs of a category; also validates the category exists
    public Set<String> getSkillIdsForCategory(String categoryId) {
        try {
            List<SkillResponse> skills = restClient.get()
                    .uri("/api/skills?categoryId={id}", categoryId)
                    .header(HttpHeaders.AUTHORIZATION, callerToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SkillResponse>>() {});
            return skills.stream().map(SkillResponse::id).collect(Collectors.toSet());
        } catch (HttpClientErrorException.NotFound e) {
            throw new InvalidStatusException("Invalid or inactive category");
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
