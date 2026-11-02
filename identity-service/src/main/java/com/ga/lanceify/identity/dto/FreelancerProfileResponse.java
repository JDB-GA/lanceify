package com.ga.lanceify.identity.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.Availability;
import com.ga.lanceify.identity.model.FreelancerProfile;

public record FreelancerProfileResponse(
		String id,
		String userId,
		String bio,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Availability availability,
		List<String> skillIds,
		ApprovalStatus approvalStatus,
		String rejectionReason,
		Instant submittedAt,
		Instant reviewedAt) {

	public static FreelancerProfileResponse from(FreelancerProfile profile) {
		return new FreelancerProfileResponse(
				profile.getId(),
				profile.getUserId(),
				profile.getBio(),
				profile.getMinPrice(),
				profile.getMaxPrice(),
				profile.getAvailability(),
				profile.getSkillIds(),
				profile.getApprovalStatus(),
				profile.getRejectionReason(),
				profile.getSubmittedAt(),
				profile.getReviewedAt());
	}

}
