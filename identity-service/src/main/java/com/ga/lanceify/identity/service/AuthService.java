package com.ga.lanceify.identity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ga.lanceify.identity.dto.AuthResponse;
import com.ga.lanceify.identity.dto.LoginRequest;
import com.ga.lanceify.identity.dto.MessageResponse;
import com.ga.lanceify.identity.dto.RegisterRequest;
import com.ga.lanceify.identity.exception.AccountBannedException;
import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.InvalidApplicationException;
import com.ga.lanceify.identity.exception.UnauthenticatedException;
import com.ga.lanceify.identity.model.User;
import com.ga.lanceify.identity.repository.UserRepository;
import com.ga.lanceify.identity.security.JwtUtils;

@Service
public class AuthService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtUtils jwtUtils;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
	}

	public MessageResponse register(RegisterRequest request) {
		if (request.role() == User.Role.ADMIN) {
			throw new InvalidApplicationException("Cannot register as ADMIN");
		}
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email is already registered");
		}
		if (userRepository.existsByUsername(request.username())) {
			throw new ConflictException("Username is already taken");
		}
		User user = User.builder()
				.firstName(request.firstName())
				.lastName(request.lastName())
				.username(request.username())
				.email(email)
				.password(passwordEncoder.encode(request.password()))
				.role(request.role())
				.build();
		userRepository.save(user);
		return new MessageResponse("Registered successfully. Please log in.");
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
				.orElseThrow(() -> new UnauthenticatedException("Invalid email or password"));
		if (user.isBanned()) {
			throw new AccountBannedException("This account has been banned");
		}
		return AuthResponse.from(jwtUtils.generateJwtToken(user), user);
	}

}
