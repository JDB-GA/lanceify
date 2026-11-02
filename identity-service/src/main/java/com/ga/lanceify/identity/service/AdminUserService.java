package com.ga.lanceify.identity.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.ResourceNotFoundException;
import com.ga.lanceify.identity.model.User;
import com.ga.lanceify.identity.repository.UserRepository;

@Service
public class AdminUserService {

	private final UserRepository repository;

	public AdminUserService(UserRepository repository) {
		this.repository = repository;
	}

	public Page<User> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public User setBanned(String id, boolean banned) {
		User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (user.getRole() == User.Role.ADMIN) {
			throw new ConflictException("An admin cannot be banned");
		}
		user.setBanned(banned);
		return repository.save(user);
	}

}
