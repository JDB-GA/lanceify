package com.ga.lanceify.identity.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.identity.dto.UserSummary;
import com.ga.lanceify.identity.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final int MAX_IDS = 100;

	private final UserRepository userRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<UserSummary> summaries(@RequestParam List<String> ids) {
		List<String> unique = ids.stream().filter(id -> !id.isBlank()).distinct().limit(MAX_IDS).toList();
		return userRepository.findAllById(unique).stream().map(UserSummary::from).toList();
	}

}
