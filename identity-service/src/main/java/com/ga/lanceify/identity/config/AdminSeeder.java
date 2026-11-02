package com.ga.lanceify.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ga.lanceify.identity.model.User;
import com.ga.lanceify.identity.repository.UserRepository;

/**
 * Admins cannot register publicly, so one is created at startup
 * from ADMIN_EMAIL and ADMIN_PASSWORD in .env. Skipped if they are not set.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final String email;

	private final String password;

	public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${admin.email:}") String email, @Value("${admin.password:}") String password) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.email = email.trim().toLowerCase();
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (!StringUtils.hasText(email) || !StringUtils.hasText(password) || userRepository.existsByEmail(email)) {
			return;
		}
		userRepository.save(User.builder()
				.firstName("Admin")
				.lastName("User")
				.username(email)
				.email(email)
				.password(passwordEncoder.encode(password))
				.role(User.Role.ADMIN)
				.build());
	}

}
