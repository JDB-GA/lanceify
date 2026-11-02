package com.ga.lanceify.identity.dto;

import com.ga.lanceify.identity.model.User;

public record AuthResponse(String token, String userId, String firstName, String lastName, String email,
		User.Role role) {

	public static AuthResponse from(String token, User user) {
		return new AuthResponse(token, user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
				user.getRole());
	}

}
