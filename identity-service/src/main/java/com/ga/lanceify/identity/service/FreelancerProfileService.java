package com.ga.lanceify.identity.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Service;

import com.ga.lanceify.identity.dto.FreelancerProfileRequest;
import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.InvalidApplicationException;
import com.ga.lanceify.identity.exception.ResourceNotFoundException;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.repository.FreelancerProfileRepository;

@Service
public class FreelancerProfileService {

	private final FreelancerProfileRepository repository;

	public FreelancerProfileService(FreelancerProfileRepository repository) {
		this.repository = repository;
	}

	public FreelancerProfile create(String userId, FreelancerProfileRequest request) {
		if (repository.existsByUserId(userId)) {
			throw new ConflictException("Freelancer profile already exists");
		}
		FreelancerProfile profile = new FreelancerProfile();
		profile.setUserId(userId);
		applyRequest(profile, request);
		return repository.save(profile);
	}

	public FreelancerProfile getByUserId(String userId) {
		return repository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
	}

	public FreelancerProfile update(String userId, FreelancerProfileRequest request) {
		FreelancerProfile profile = getByUserId(userId);
		requireEditable(profile);
		applyRequest(profile, request);
		return repository.save(profile);
	}

	public void withdraw(String userId) {
		FreelancerProfile profile = getByUserId(userId);
		if (profile.getApprovalStatus() == ApprovalStatus.APPROVED) {
			throw new ConflictException("An approved profile cannot be withdrawn");
		}
		repository.delete(profile);
	}

	public FreelancerProfile submit(String userId) {
		FreelancerProfile profile = getByUserId(userId);
		requireEditable(profile);
		requireComplete(profile);
		profile.setApprovalStatus(ApprovalStatus.PENDING);
		profile.setRejectionReason(null);
		profile.setSubmittedAt(Instant.now());
		profile.setReviewedAt(null);
		return repository.save(profile);
	}

	private void applyRequest(FreelancerProfile profile, FreelancerProfileRequest request) {
		profile.setBio(request.bio());
		profile.setMinPrice(request.minPrice());
		profile.setMaxPrice(request.maxPrice());
		profile.setSkillIds(request.skillIds() == null
				? new ArrayList<>()
				: new ArrayList<>(new LinkedHashSet<>(request.skillIds())));
	}

	private void requireEditable(FreelancerProfile profile) {
		ApprovalStatus status = profile.getApprovalStatus();
		if (status != ApprovalStatus.DRAFT && status != ApprovalStatus.REJECTED) {
			throw new ConflictException("Profile cannot be changed while " + status);
		}
	}

	private void requireComplete(FreelancerProfile profile) {
		if (profile.getBio() == null || profile.getBio().isBlank()) {
			throw new InvalidApplicationException("A bio is required before submitting");
		}
		if (profile.getMinPrice() == null || profile.getMaxPrice() == null) {
			throw new InvalidApplicationException("A price range is required before submitting");
		}
		if (profile.getSkillIds().isEmpty()) {
			throw new InvalidApplicationException("At least one skill is required before submitting");
		}
	}

}
