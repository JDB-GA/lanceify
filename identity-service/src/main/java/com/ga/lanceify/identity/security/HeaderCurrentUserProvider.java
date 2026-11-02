package com.ga.lanceify.identity.security;

import org.springframework.stereotype.Component;

import com.ga.lanceify.identity.exception.UnauthenticatedException;

import jakarta.servlet.http.HttpServletRequest;

// Temporary: trusts the X-User-Id header. Replace with a JWT-based provider before release.
@Component
public class HeaderCurrentUserProvider implements CurrentUserProvider {

	private final HttpServletRequest request;

	public HeaderCurrentUserProvider(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getUserId() {
		String userId = request.getHeader("X-User-Id");
		if (userId == null || userId.isBlank()) {
			throw new UnauthenticatedException("Authentication is required");
		}
		return userId;
	}

}
