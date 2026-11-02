package com.ga.lanceify.identity.dto;

import com.ga.lanceify.identity.model.User;

public record UserSummary(String id, String firstName, String lastName, String username, User.Role role) {

	public static UserSummary from(User user) {
		return new UserSummary(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername(),
				user.getRole());
	}

}
