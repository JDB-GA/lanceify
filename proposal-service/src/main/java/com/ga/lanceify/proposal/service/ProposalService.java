package com.ga.lanceify.proposal.service;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ga.lanceify.proposal.client.ProjectClient;
import com.ga.lanceify.proposal.dto.ApplyRequest;
import com.ga.lanceify.proposal.dto.ProjectSummary;
import com.ga.lanceify.proposal.exception.ConflictException;
import com.ga.lanceify.proposal.exception.ResourceNotFoundException;
import com.ga.lanceify.proposal.model.Proposal;
import com.ga.lanceify.proposal.model.Proposal.ProposalStatus;
import com.ga.lanceify.proposal.repository.ProposalRepository;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProjectClient projectClient;
    private final MongoTemplate mongoTemplate;

    public ProposalService(ProposalRepository proposalRepository, ProjectClient projectClient,
            MongoTemplate mongoTemplate) {
        this.proposalRepository = proposalRepository;
        this.projectClient = projectClient;
        this.mongoTemplate = mongoTemplate;
    }

    public Proposal apply(String freelancerId, String projectId, ApplyRequest request) {
        ProjectSummary project = projectClient.getProject(projectId);
        if (!project.isOpen() || proposalRepository.existsByProjectIdAndStatus(projectId, ProposalStatus.ACCEPTED)) {
            throw new ConflictException("This project is no longer accepting proposals");
        }
        if (freelancerId.equals(project.getClientId())) {
            throw new ConflictException("You cannot apply to your own project");
        }
        if (proposalRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            throw new ConflictException("You have already applied to this project");
        }

        Proposal proposal = new Proposal();
        proposal.setProjectId(projectId);
        proposal.setClientId(project.getClientId());
        proposal.setProjectTitle(project.getTitle());
        proposal.setFreelancerId(freelancerId);
        proposal.setCoverLetter(request.getCoverLetter().trim());
        proposal.setBidAmount(request.getBidAmount());
        proposal.setDeliveryDays(request.getDeliveryDays());
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.insert(proposal);
    }

    public List<Proposal> myProposals(String freelancerId) {
        return proposalRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId);
    }

    public Proposal withdraw(String freelancerId, String proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .filter(found -> found.getFreelancerId().equals(freelancerId))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        requirePending(proposal, "Only pending proposals can be withdrawn");
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal.setDecidedAt(Instant.now());
        return proposalRepository.save(proposal);
    }

    public List<Proposal> receivedProposals(String clientId) {
        return proposalRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public List<Proposal> projectProposals(String clientId, String projectId) {
        return proposalRepository.findByProjectIdAndClientIdOrderByCreatedAtDesc(projectId, clientId);
    }

    public Proposal accept(String clientId, String proposalId, Long expectedVersion) {
        Proposal proposal = findForClient(clientId, proposalId, expectedVersion);
        requirePending(proposal, "Only pending proposals can be accepted");

        ProjectSummary project = projectClient.getProject(proposal.getProjectId());
        if (!clientId.equals(project.getClientId())) {
            throw new ResourceNotFoundException("Proposal not found");
        }
        if (!project.isOpen()) {
            throw new ConflictException("This project is no longer open");
        }

        Instant now = Instant.now();
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setDecidedAt(now);
        Proposal accepted;
        try {
            accepted = proposalRepository.save(proposal);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Another proposal has already been accepted for this project");
        }

        Query others = Query.query(Criteria.where("projectId").is(accepted.getProjectId())
                .and("status").is(ProposalStatus.PENDING)
                .and("_id").ne(accepted.getId()));
        Update decline = new Update()
                .set("status", ProposalStatus.REJECTED)
                .set("decidedAt", now)
                .set("updatedAt", now)
                .inc("version", 1);
        mongoTemplate.updateMulti(others, decline, Proposal.class);
        return accepted;
    }

    public Proposal reject(String clientId, String proposalId, Long expectedVersion) {
        Proposal proposal = findForClient(clientId, proposalId, expectedVersion);
        requirePending(proposal, "Only pending proposals can be rejected");
        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setDecidedAt(Instant.now());
        return proposalRepository.save(proposal);
    }

    private Proposal findForClient(String clientId, String proposalId, Long expectedVersion) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .filter(found -> clientId.equals(found.getClientId()))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        if (expectedVersion != null && !expectedVersion.equals(proposal.getVersion())) {
            throw new ConflictException("This proposal was just changed, please refresh and try again");
        }
        return proposal;
    }

    private void requirePending(Proposal proposal, String message) {
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ConflictException(message);
        }
    }

}
