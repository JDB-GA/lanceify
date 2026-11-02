package com.ga.lanceify.identity.dto;

import com.ga.lanceify.identity.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
		@NotBlank(message = "Username is required") @Size(min = 3, max = 30) String username,
		@NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
		@NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
		@NotNull(message = "Role is required") User.Role role) {
}
