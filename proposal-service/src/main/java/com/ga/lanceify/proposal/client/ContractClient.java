package com.ga.lanceify.proposal.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ga.lanceify.proposal.dto.ContractSummary;
import com.ga.lanceify.proposal.exception.ConflictException;

@Component
public class ContractClient {

	private final RestClient restClient;

	public ContractClient(@Value("${contract.service.url}") String url) {
		this.restClient = RestClient.create(url);
	}

	public ContractSummary create(String proposalId) {
		try {
			return restClient.post()
					.uri("/api/contracts")
					.header(HttpHeaders.AUTHORIZATION, callerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("proposalId", proposalId))
					.retrieve()
					.body(ContractSummary.class);
		} catch (HttpClientErrorException.Conflict e) {
			throw new ConflictException("This freelancer already has the maximum number of active contracts");
		}
	}

	public void cancelWithoutProject(String contractId) {
		restClient.put()
				.uri("/api/contracts/{id}/cancel?updateProject=false", contractId)
				.header(HttpHeaders.AUTHORIZATION, callerToken())
				.retrieve()
				.toBodilessEntity();
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
