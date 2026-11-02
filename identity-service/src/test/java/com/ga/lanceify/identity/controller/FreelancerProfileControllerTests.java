package com.ga.lanceify.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.ResourceNotFoundException;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.security.HeaderCurrentUserProvider;
import com.ga.lanceify.identity.service.FreelancerProfileService;

@WebMvcTest(FreelancerProfileController.class)
@Import(HeaderCurrentUserProvider.class)
class FreelancerProfileControllerTests {

	private static final String VALID = "{\"bio\":\"Java developer\",\"minPrice\":50,\"maxPrice\":500,\"skillIds\":[\"java\"]}";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private FreelancerProfileService service;

	@Test
	void createReturns201() throws Exception {
		FreelancerProfile profile = new FreelancerProfile();
		profile.setUserId("u1");
		when(service.create(eq("u1"), any())).thenReturn(profile);
		mvc.perform(post("/api/freelancers/me").header("X-User-Id", "u1")
				.contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.approvalStatus").value("DRAFT"));
	}

	@Test
	void missingUserReturns401() throws Exception {
		mvc.perform(get("/api/freelancers/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void invertedPriceRangeReturns400() throws Exception {
		mvc.perform(post("/api/freelancers/me").header("X-User-Id", "u1")
				.contentType(MediaType.APPLICATION_JSON).content("{\"minPrice\":500,\"maxPrice\":50}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]").value("minPrice must not be greater than maxPrice"));
	}

	@Test
	void missingProfileReturns404() throws Exception {
		when(service.getByUserId("u1")).thenThrow(new ResourceNotFoundException("Freelancer profile not found"));
		mvc.perform(get("/api/freelancers/me").header("X-User-Id", "u1")).andExpect(status().isNotFound());
	}

	@Test
	void conflictReturns409() throws Exception {
		when(service.submit("u1")).thenThrow(new ConflictException("Profile cannot be changed while PENDING"));
		mvc.perform(post("/api/freelancers/me/submit").header("X-User-Id", "u1")).andExpect(status().isConflict());
	}

}
