package com.ga.lanceify.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ga.lanceify.identity.dto.FreelancerProfileRequest;
import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.InvalidApplicationException;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.repository.FreelancerProfileRepository;

class FreelancerApplicationFlowTests {

	private FreelancerProfileRepository repository;
	private FreelancerProfileService profiles;
	private FreelancerApplicationReviewService reviews;
	private FreelancerProfile profile;

	@BeforeEach
	void setUp() {
		repository = mock(FreelancerProfileRepository.class);
		profiles = new FreelancerProfileService(repository);
		reviews = new FreelancerApplicationReviewService(repository);
		profile = new FreelancerProfile();
		profile.setUserId("u1");
		profile.setBio("Java developer");
		profile.setMinPrice(new BigDecimal("50"));
		profile.setMaxPrice(new BigDecimal("500"));
		profile.setSkillIds(List.of("java"));
		lenient().when(repository.findByUserId("u1")).thenReturn(Optional.of(profile));
		lenient().when(repository.findById("p1")).thenReturn(Optional.of(profile));
		lenient().when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
	}

	@Test
	void createRejectsSecondProfile() {
		when(repository.existsByUserId("u1")).thenReturn(true);
		FreelancerProfileRequest request = new FreelancerProfileRequest("bio", null, null, null);
		assertThrows(ConflictException.class, () -> profiles.create("u1", request));
	}

	@Test
	void createStartsAsDraftAndRemovesDuplicateSkills() {
		FreelancerProfileRequest request = new FreelancerProfileRequest("bio", null, null, List.of("a", "a", "b"));
		FreelancerProfile created = profiles.create("u2", request);
		assertEquals(ApprovalStatus.DRAFT, created.getApprovalStatus());
		assertEquals(List.of("a", "b"), created.getSkillIds());
	}

	@Test
	void submitMovesDraftToPending() {
		assertEquals(ApprovalStatus.PENDING, profiles.submit("u1").getApprovalStatus());
	}

	@Test
	void submitRequiresSkills() {
		profile.setSkillIds(List.of());
		assertThrows(InvalidApplicationException.class, () -> profiles.submit("u1"));
	}

	@Test
	void pendingProfileCannotBeEdited() {
		profile.setApprovalStatus(ApprovalStatus.PENDING);
		FreelancerProfileRequest request = new FreelancerProfileRequest("new", null, null, null);
		assertThrows(ConflictException.class, () -> profiles.update("u1", request));
	}

	@Test
	void rejectedProfileCanResubmitAndClearsReason() {
		profile.setApprovalStatus(ApprovalStatus.REJECTED);
		profile.setRejectionReason("Too short");
		FreelancerProfile submitted = profiles.submit("u1");
		assertEquals(ApprovalStatus.PENDING, submitted.getApprovalStatus());
		assertNull(submitted.getRejectionReason());
	}

	@Test
	void approvedProfileCannotBeWithdrawn() {
		profile.setApprovalStatus(ApprovalStatus.APPROVED);
		assertThrows(ConflictException.class, () -> profiles.withdraw("u1"));
		verify(repository, never()).delete(any());
	}

	@Test
	void approveRequiresPending() {
		assertThrows(ConflictException.class, () -> reviews.approve("p1"));
		profile.setApprovalStatus(ApprovalStatus.PENDING);
		assertEquals(ApprovalStatus.APPROVED, reviews.approve("p1").getApprovalStatus());
	}

	@Test
	void rejectStoresReason() {
		profile.setApprovalStatus(ApprovalStatus.PENDING);
		FreelancerProfile rejected = reviews.reject("p1", "Incomplete");
		assertEquals(ApprovalStatus.REJECTED, rejected.getApprovalStatus());
		assertEquals("Incomplete", rejected.getRejectionReason());
	}

}
