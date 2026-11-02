package com.ga.lanceify.contract.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.contract.dto.CreateContractRequest;
import com.ga.lanceify.contract.model.Contract;
import com.ga.lanceify.contract.service.ContractService;

import jakarta.validation.Valid;

@RestController
public class ContractController {

	private final ContractService service;

	public ContractController(ContractService service) {
		this.service = service;
	}

	@PostMapping("/api/contracts")
	@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Contract create(@Valid @RequestBody CreateContractRequest request, Authentication auth) {
		return service.createFromProposal(request.proposalId(), auth.getName(), isAdmin(auth));
	}

	@GetMapping("/api/contracts/my")
	@PreAuthorize("hasAnyRole('CLIENT','FREELANCER')")
	public List<Contract> mine(Authentication auth) {
		return service.forUser(auth.getName(), hasRole(auth, "ROLE_CLIENT"));
	}

	@GetMapping("/api/contracts/{id}")
	public Contract get(@PathVariable String id, Authentication auth) {
		return service.get(id, auth.getName(), isAdmin(auth));
	}

	@PutMapping("/api/contracts/{id}/complete")
	@PreAuthorize("hasRole('CLIENT')")
	public Contract complete(@PathVariable String id, Authentication auth) {
		return service.complete(id, auth.getName());
	}

	@PutMapping("/api/contracts/{id}/cancel")
	@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
	public Contract cancel(@PathVariable String id,
			@RequestParam(defaultValue = "true") boolean updateProject, Authentication auth) {
		return service.cancel(id, auth.getName(), isAdmin(auth), updateProject);
	}

	@GetMapping("/api/admin/contracts")
	@PreAuthorize("hasRole('ADMIN')")
	public List<Contract> all() {
		return service.all();
	}

	private boolean isAdmin(Authentication auth) {
		return hasRole(auth, "ROLE_ADMIN");
	}

	private boolean hasRole(Authentication auth, String role) {
		return auth.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(role));
	}

}
