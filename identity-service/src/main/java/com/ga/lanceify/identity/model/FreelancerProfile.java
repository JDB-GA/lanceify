package com.ga.lanceify.identity.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "freelancer_profiles")
public class FreelancerProfile {

	@Id
	private String id;

	@Indexed(unique = true)
	private String userId;

	private String bio;

	@Field(targetType = FieldType.DECIMAL128)
	private BigDecimal minPrice;

	@Field(targetType = FieldType.DECIMAL128)
	private BigDecimal maxPrice;

	private Availability availability = Availability.AVAILABLE;

	private List<String> skillIds = new ArrayList<>();

	private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;

	private String rejectionReason;

	private Instant submittedAt;

	private Instant reviewedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(BigDecimal minPrice) {
		this.minPrice = minPrice;
	}

	public BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(BigDecimal maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Availability getAvailability() {
		return availability;
	}

	public void setAvailability(Availability availability) {
		this.availability = availability;
	}

	public List<String> getSkillIds() {
		return skillIds;
	}

	public void setSkillIds(List<String> skillIds) {
		this.skillIds = skillIds;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(Instant submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(Instant reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

}
