package com.ga.lanceify.proposal.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ga.lanceify.proposal.model.Proposal;

public interface ProposalRepository extends MongoRepository<Proposal, String> {

    List<Proposal> findByFreelancerIdOrderByCreatedAtDesc(String freelancerId);

    boolean existsByProjectIdAndFreelancerId(String projectId, String freelancerId);

}
