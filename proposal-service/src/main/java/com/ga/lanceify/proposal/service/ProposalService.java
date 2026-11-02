package com.ga.lanceify.proposal.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ga.lanceify.proposal.client.ContractClient;
import com.ga.lanceify.proposal.client.ProjectClient;
import com.ga.lanceify.proposal.dto.ApplyRequest;
import com.ga.lanceify.proposal.dto.ContractSummary;
import com.ga.lanceify.proposal.dto.ProjectSummary;
import com.ga.lanceify.proposal.exception.ConflictException;
import com.ga.lanceify.proposal.exception.ResourceNotFoundException;
import com.ga.lanceify.proposal.model.Proposal;
import com.ga.lanceify.proposal.model.Proposal.ProposalStatus;
import com.ga.lanceify.proposal.repository.ProposalRepository;

@Service
public class ProposalService {

    public static final int MAX_PROPOSALS_PER_PROJECT = 5;

    private static final List<ProposalStatus> ACTIVE_STATUSES = List.of(ProposalStatus.PENDING, ProposalStatus.ACCEPTED);

    private final ProposalRepository proposalRepository;
    private final ProjectClient projectClient;
    private final ContractClient contractClient;
    private final MongoTemplate mongoTemplate;
    private final Map<String, ReentrantLock> projectLocks = new ConcurrentHashMap<>();

    public ProposalService(ProposalRepository proposalRepository, ProjectClient projectClient,
            ContractClient contractClient, MongoTemplate mongoTemplate) {
        this.proposalRepository = proposalRepository;
        this.projectClient = projectClient;
        this.contractClient = contractClient;
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

        Proposal proposal = new Proposal();
        proposal.setProjectId(projectId);
        proposal.setClientId(project.getClientId());
        proposal.setProjectTitle(project.getTitle());
        proposal.setFreelancerId(freelancerId);
        proposal.setCoverLetter(request.getCoverLetter().trim());
        proposal.setBidAmount(request.getBidAmount());
        proposal.setDeliveryDays(request.getDeliveryDays());
        proposal.setStatus(ProposalStatus.PENDING);

        ReentrantLock lock = projectLocks.computeIfAbsent(projectId, id -> new ReentrantLock());
        lock.lock();
        try {
            if (proposalRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
                throw new ConflictException("You have already applied to this project");
            }
            long active = proposalRepository.countByProjectIdAndStatusIn(projectId, ACTIVE_STATUSES);
            if (active >= MAX_PROPOSALS_PER_PROJECT) {
                throw new ConflictException("This project already has the maximum of "
                        + MAX_PROPOSALS_PER_PROJECT + " proposals");
            }
            return proposalRepository.insert(proposal);
        } finally {
            lock.unlock();
        }
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

    public Proposal get(String proposalId, String userId, boolean admin) {
        return proposalRepository.findById(proposalId)
                .filter(found -> admin || userId.equals(found.getClientId()) || userId.equals(found.getFreelancerId()))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
    }

    public Proposal accept(String clientId, String proposalId, Long expectedVersion) {
        Proposal proposal = findForClient(clientId, proposalId, expectedVersion);
        requirePending(proposal, "Only pending proposals can be accepted");

        ProjectSummary project = projectClient.getProject(proposal.getProjectId());
        if (!clientId.equals(project.getClientId())) {
            throw new ResourceNotFoundException("Proposal not found");
        }
        return hire(proposal, project);
    }

    public Proposal approveAsAdmin(String proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        requirePending(proposal, "Only pending proposals can be approved");
        return hire(proposal, projectClient.getProject(proposal.getProjectId()));
    }

    private Proposal hire(Proposal proposal, ProjectSummary project) {
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

        ContractSummary contract;
        try {
            contract = contractClient.create(accepted.getId());
        } catch (RuntimeException e) {
            revert(accepted);
            throw e;
        }

        try {
            projectClient.assign(accepted.getProjectId(), accepted.getFreelancerId());
        } catch (RuntimeException e) {
            contractClient.cancelWithoutProject(contract.id());
            revert(accepted);
            throw new ConflictException("This project could not be assigned");
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

    private void revert(Proposal proposal) {
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setDecidedAt(null);
        proposalRepository.save(proposal);
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
