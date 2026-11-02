package com.ga.lanceify.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.ResourceNotFoundException;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.model.User;
import com.ga.lanceify.identity.repository.UserRepository;
import com.ga.lanceify.identity.security.JwtCurrentUserProvider;
import com.ga.lanceify.identity.security.JwtUtils;
import com.ga.lanceify.identity.security.MyUserDetailsService;
import com.ga.lanceify.identity.security.SecurityConfiguration;
import com.ga.lanceify.identity.service.FreelancerApplicationReviewService;
import com.ga.lanceify.identity.service.FreelancerProfileService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@WebMvcTest({ FreelancerProfileController.class, FreelancerApplicationAdminController.class })
@Import({ SecurityConfiguration.class, JwtUtils.class, MyUserDetailsService.class, JwtCurrentUserProvider.class })
class FreelancerApplicationSecurityTests {

	private static final String SECRET = "test-secret-test-secret-test-secret-1234";
	private static final String VALID = "{\"bio\":\"Java developer\",\"minPrice\":50,\"maxPrice\":500,\"skillIds\":[\"java\"]}";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private FreelancerProfileService profiles;

	@MockitoBean
	private FreelancerApplicationReviewService reviews;

	@MockitoBean
	private UserRepository users;

	@BeforeEach
	void stubUsers() {
		when(users.findById(any())).thenAnswer(call -> {
			String id = call.getArgument(0);
			User.Role role = id.startsWith("a") ? User.Role.ADMIN
					: id.startsWith("c") ? User.Role.CLIENT : User.Role.FREELANCER;
			return Optional.of(User.builder().id(id).email(id + "@test.com").role(role)
					.banned(id.startsWith("b")).build());
		});
	}

	private String bearer(String userId, String role, String secret) {
		return "Bearer " + Jwts.builder().subject(userId).claim("role", role)
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
	}

	private String freelancer() {
		return bearer("u1", "FREELANCER", SECRET);
	}

	private String admin() {
		return bearer("a1", "ADMIN", SECRET);
	}

	@Test
	void noTokenReturns401() throws Exception {
		mvc.perform(get("/api/freelancers/me")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/admin/freelancer-applications")).andExpect(status().isUnauthorized());
	}

	@Test
	void bannedUserTokenReturns401() throws Exception {
		mvc.perform(get("/api/freelancers/me").header(HttpHeaders.AUTHORIZATION, bearer("b1", "FREELANCER", SECRET)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void forgedTokenReturns401() throws Exception {
		String forged = bearer("u1", "ADMIN", "another-secret-another-secret-another-1");
		mvc.perform(get("/api/admin/freelancer-applications").header(HttpHeaders.AUTHORIZATION, forged))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void freelancerCannotUseAdminEndpoints() throws Exception {
		mvc.perform(put("/api/admin/freelancer-applications/p1/approve").header(HttpHeaders.AUTHORIZATION, freelancer()))
				.andExpect(status().isForbidden());
	}

	@Test
	void clientCannotCreateProfile() throws Exception {
		mvc.perform(post("/api/freelancers/me").header(HttpHeaders.AUTHORIZATION, bearer("c1", "CLIENT", SECRET))
				.contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isForbidden());
	}

	@Test
	void createUsesUserIdFromToken() throws Exception {
		FreelancerProfile profile = new FreelancerProfile();
		profile.setUserId("u1");
		when(profiles.create(eq("u1"), any())).thenReturn(profile);
		mvc.perform(post("/api/freelancers/me").header(HttpHeaders.AUTHORIZATION, freelancer())
				.contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value("u1"))
				.andExpect(jsonPath("$.approvalStatus").value("DRAFT"));
	}

	@Test
	void adminCanApprove() throws Exception {
		FreelancerProfile profile = new FreelancerProfile();
		profile.setApprovalStatus(ApprovalStatus.APPROVED);
		when(reviews.approve("p1")).thenReturn(profile);
		mvc.perform(put("/api/admin/freelancer-applications/p1/approve").header(HttpHeaders.AUTHORIZATION, admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.approvalStatus").value("APPROVED"));
	}

	@Test
	void rejectWithoutReasonReturns400() throws Exception {
		mvc.perform(put("/api/admin/freelancer-applications/p1/reject").header(HttpHeaders.AUTHORIZATION, admin())
				.contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void invertedPriceRangeReturns400() throws Exception {
		mvc.perform(post("/api/freelancers/me").header(HttpHeaders.AUTHORIZATION, freelancer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"minPrice\":500,\"maxPrice\":50}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]").value("minPrice must not be greater than maxPrice"));
	}

	@Test
	void missingProfileReturns404() throws Exception {
		when(profiles.getByUserId("u1")).thenThrow(new ResourceNotFoundException("Freelancer profile not found"));
		mvc.perform(get("/api/freelancers/me").header(HttpHeaders.AUTHORIZATION, freelancer()))
				.andExpect(status().isNotFound());
	}

	@Test
	void conflictReturns409() throws Exception {
		when(profiles.submit("u1")).thenThrow(new ConflictException("Profile cannot be changed while PENDING"));
		mvc.perform(post("/api/freelancers/me/submit").header(HttpHeaders.AUTHORIZATION, freelancer()))
				.andExpect(status().isConflict());
	}

}
