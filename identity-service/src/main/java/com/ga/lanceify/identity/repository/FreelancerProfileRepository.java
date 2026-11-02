package com.ga.lanceify.identity.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;

public interface FreelancerProfileRepository extends MongoRepository<FreelancerProfile, String> {

	Optional<FreelancerProfile> findByUserId(String userId);

	boolean existsByUserId(String userId);

	Page<FreelancerProfile> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

}
