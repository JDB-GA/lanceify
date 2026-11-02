package com.ga.lanceify.proposal.service;

import java.util.List;

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

    public ProposalService(ProposalRepository proposalRepository, ProjectClient projectClient) {
        this.proposalRepository = proposalRepository;
        this.projectClient = projectClient;
    }

    public Proposal apply(String freelancerId, String projectId, ApplyRequest request) {
        ProjectSummary project = projectClient.getProject(projectId);
        if (!project.isOpen()) {
            throw new ConflictException("This project is no longer accepting proposals");
        }
        if (proposalRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            throw new ConflictException("You have already applied to this project");
        }

        Proposal proposal = new Proposal();
        proposal.setProjectId(projectId);
        proposal.setClientId(project.getClientId());
        proposal.setFreelancerId(freelancerId);
        proposal.setCoverLetter(request.getCoverLetter().trim());
        proposal.setBidAmount(request.getBidAmount());
        proposal.setDeliveryDays(request.getDeliveryDays());
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    public List<Proposal> myProposals(String freelancerId) {
        return proposalRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId);
    }

    public Proposal withdraw(String freelancerId, String proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .filter(found -> found.getFreelancerId().equals(freelancerId))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ConflictException("Only pending proposals can be withdrawn");
        }

        proposal.setStatus(ProposalStatus.WITHDRAWN);
        return proposalRepository.save(proposal);
    }

}
