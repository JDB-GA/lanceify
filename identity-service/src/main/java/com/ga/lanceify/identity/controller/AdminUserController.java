package com.ga.lanceify.identity.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.identity.dto.AdminUserResponse;
import com.ga.lanceify.identity.dto.PageResponse;
import com.ga.lanceify.identity.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService service;

	public AdminUserController(AdminUserService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<AdminUserResponse> list(Pageable pageable) {
		return PageResponse.from(service.list(pageable), AdminUserResponse::from);
	}

	@PutMapping("/{id}/ban")
	public AdminUserResponse ban(@PathVariable String id) {
		return AdminUserResponse.from(service.setBanned(id, true));
	}

	@PutMapping("/{id}/unban")
	public AdminUserResponse unban(@PathVariable String id) {
		return AdminUserResponse.from(service.setBanned(id, false));
	}

}
