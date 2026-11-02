package com.ga.lanceify.identity.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.identity.dto.ContactResponse;
import com.ga.lanceify.identity.service.ContactService;

@RestController
@RequestMapping("/api/users/contacts")
public class ContactController {

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@GetMapping("/freelancers")
	@PreAuthorize("hasRole('CLIENT')")
	public List<ContactResponse> freelancers() {
		return contactService.approvedFreelancers();
	}

	@GetMapping("/clients")
	@PreAuthorize("hasRole('FREELANCER')")
	public List<ContactResponse> clients() {
		return contactService.clients();
	}

}
