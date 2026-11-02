package com.ga.lanceify.contract.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.ga.lanceify.contract.dto.ProposalSummary;

@Component
public class ProposalClient {

	private final RestClient restClient;

	public ProposalClient(@Value("${proposal.service.url}") String url) {
		this.restClient = RestClient.create(url);
	}

	public ProposalSummary get(String proposalId) {
		try {
			return restClient.get()
					.uri("/api/proposals/{id}", proposalId)
					.header(HttpHeaders.AUTHORIZATION, CallerToken.header())
					.retrieve()
					.body(ProposalSummary.class);
		} catch (HttpClientErrorException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found");
		}
	}

}
