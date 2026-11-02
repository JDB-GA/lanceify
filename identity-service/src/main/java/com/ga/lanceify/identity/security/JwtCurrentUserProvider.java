// package com.ga.lanceify.identity.security;
//
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
//
// import com.ga.lanceify.identity.exception.UnauthenticatedException;
//
// @Component
// public class JwtCurrentUserProvider implements CurrentUserProvider {
//
// 	@Override
// 	public String getUserId() {
// 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// 		if (authentication == null || !authentication.isAuthenticated()) {
// 			throw new UnauthenticatedException("Authentication is required");
// 		}
// 		return authentication.getName();
// 	}
//
// }
