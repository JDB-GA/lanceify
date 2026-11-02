package com.ga.lanceify.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.identity.dto.FreelancerProfileRequest;
import com.ga.lanceify.identity.dto.FreelancerProfileResponse;
import com.ga.lanceify.identity.security.CurrentUserProvider;
import com.ga.lanceify.identity.service.FreelancerProfileService;

import jakarta.validation.Valid;

@PreAuthorize("hasRole('FREELANCER')")
@RestController
@RequestMapping("/api/freelancers/me")
public class FreelancerProfileController {

	private final FreelancerProfileService service;
	private final CurrentUserProvider currentUser;

	public FreelancerProfileController(FreelancerProfileService service, CurrentUserProvider currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FreelancerProfileResponse create(@Valid @RequestBody FreelancerProfileRequest request) {
		return FreelancerProfileResponse.from(service.create(currentUser.getUserId(), request));
	}

	@GetMapping
	public FreelancerProfileResponse get() {
		return FreelancerProfileResponse.from(service.getByUserId(currentUser.getUserId()));
	}

	@PutMapping
	public FreelancerProfileResponse update(@Valid @RequestBody FreelancerProfileRequest request) {
		return FreelancerProfileResponse.from(service.update(currentUser.getUserId(), request));
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void withdraw() {
		service.withdraw(currentUser.getUserId());
	}

	@PostMapping("/submit")
	public FreelancerProfileResponse submit() {
		return FreelancerProfileResponse.from(service.submit(currentUser.getUserId()));
	}

}
