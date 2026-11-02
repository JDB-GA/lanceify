package com.ga.lanceify.identity.dto;

import com.ga.lanceify.identity.model.User;

public record AdminUserResponse(String id, String firstName, String lastName, String email, User.Role role,
		boolean banned) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
				user.getRole(), user.isBanned());
	}

}
