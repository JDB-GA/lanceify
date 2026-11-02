package com.ga.lanceify.identity.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ga.lanceify.identity.exception.ConflictException;
import com.ga.lanceify.identity.exception.ResourceNotFoundException;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.repository.FreelancerProfileRepository;

@Service
public class FreelancerApplicationReviewService {

	private final FreelancerProfileRepository repository;

	public FreelancerApplicationReviewService(FreelancerProfileRepository repository) {
		this.repository = repository;
	}

	public Page<FreelancerProfile> list(ApprovalStatus status, Pageable pageable) {
		return status == null
				? repository.findAll(pageable)
				: repository.findByApprovalStatus(status, pageable);
	}

	public FreelancerProfile get(String id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));
	}

	public FreelancerProfile approve(String id) {
		FreelancerProfile profile = getPending(id);
		profile.setApprovalStatus(ApprovalStatus.APPROVED);
		profile.setRejectionReason(null);
		profile.setReviewedAt(Instant.now());
		return repository.save(profile);
	}

	public FreelancerProfile reject(String id, String reason) {
		FreelancerProfile profile = getPending(id);
		profile.setApprovalStatus(ApprovalStatus.REJECTED);
		profile.setRejectionReason(reason);
		profile.setReviewedAt(Instant.now());
		return repository.save(profile);
	}

	private FreelancerProfile getPending(String id) {
		FreelancerProfile profile = get(id);
		if (profile.getApprovalStatus() != ApprovalStatus.PENDING) {
			throw new ConflictException("Only a pending application can be reviewed");
		}
		return profile;
	}

}
