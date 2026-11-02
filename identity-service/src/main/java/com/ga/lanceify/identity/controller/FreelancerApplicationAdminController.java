package com.ga.lanceify.identity.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.identity.dto.FreelancerProfileResponse;
import com.ga.lanceify.identity.dto.PageResponse;
import com.ga.lanceify.identity.dto.RejectApplicationRequest;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.service.FreelancerApplicationReviewService;

import jakarta.validation.Valid;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/freelancer-applications")
public class FreelancerApplicationAdminController {

	private final FreelancerApplicationReviewService service;

	public FreelancerApplicationAdminController(FreelancerApplicationReviewService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<FreelancerProfileResponse> list(
			@RequestParam(required = false) ApprovalStatus status,
			@PageableDefault(sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponse.from(service.list(status, pageable), FreelancerProfileResponse::from);
	}

	@GetMapping("/{id}")
	public FreelancerProfileResponse get(@PathVariable String id) {
		return FreelancerProfileResponse.from(service.get(id));
	}

	@PutMapping("/{id}/approve")
	public FreelancerProfileResponse approve(@PathVariable String id) {
		return FreelancerProfileResponse.from(service.approve(id));
	}

	@PutMapping("/{id}/reject")
	public FreelancerProfileResponse reject(@PathVariable String id,
			@Valid @RequestBody RejectApplicationRequest request) {
		return FreelancerProfileResponse.from(service.reject(id, request.reason()));
	}

}
