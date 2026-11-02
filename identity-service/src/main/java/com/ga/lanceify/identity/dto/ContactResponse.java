package com.ga.lanceify.identity.dto;

import com.ga.lanceify.identity.model.User;

public record ContactResponse(String id, String firstName, String lastName, User.Role role) {

	public static ContactResponse from(User user) {
		return new ContactResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getRole());
	}

}
