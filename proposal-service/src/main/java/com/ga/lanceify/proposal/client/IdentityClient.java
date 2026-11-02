package com.ga.lanceify.proposal.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class IdentityClient {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record Profile(String approvalStatus) {
	}

	private final RestClient restClient;

	public IdentityClient(@Value("${identity.service.url}") String url) {
		this.restClient = RestClient.create(url);
	}

	public boolean isCallerApproved() {
		try {
			Profile profile = restClient.get()
					.uri("/api/freelancers/me")
					.header(HttpHeaders.AUTHORIZATION, callerToken())
					.retrieve()
					.body(Profile.class);
			return profile != null && "APPROVED".equals(profile.approvalStatus());
		} catch (HttpClientErrorException e) {
			return false;
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
