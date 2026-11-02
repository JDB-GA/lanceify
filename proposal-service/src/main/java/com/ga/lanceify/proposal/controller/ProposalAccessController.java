package com.ga.lanceify.proposal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.proposal.dto.ProposalResponse;
import com.ga.lanceify.proposal.service.ProposalService;

@RestController
public class ProposalAccessController {

	private final ProposalService service;

	public ProposalAccessController(ProposalService service) {
		this.service = service;
	}

	@GetMapping("/api/proposals/{id}")
	public ProposalResponse get(@PathVariable String id, Authentication auth) {
		boolean admin = auth.getAuthorities().stream().anyMatch(item -> item.getAuthority().equals("ROLE_ADMIN"));
		return ProposalResponse.from(service.get(id, auth.getName(), admin));
	}

	@PutMapping("/api/admin/proposals/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public ProposalResponse approve(@PathVariable String id) {
		return ProposalResponse.from(service.approveAsAdmin(id));
	}

}
