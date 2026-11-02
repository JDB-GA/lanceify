package com.ga.lanceify.proposal.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ga.lanceify.proposal.model.Proposal;
import com.ga.lanceify.proposal.model.Proposal.ProposalStatus;

public interface ProposalRepository extends MongoRepository<Proposal, String> {

    List<Proposal> findByFreelancerIdOrderByCreatedAtDesc(String freelancerId);

    List<Proposal> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<Proposal> findByProjectIdAndClientIdOrderByCreatedAtDesc(String projectId, String clientId);

    boolean existsByProjectIdAndFreelancerId(String projectId, String freelancerId);

    long countByProjectIdAndStatusIn(String projectId, List<ProposalStatus> statuses);

    boolean existsByProjectIdAndStatus(String projectId, ProposalStatus status);

}
