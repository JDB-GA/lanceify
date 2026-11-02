package com.ga.lanceify.contract.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ga.lanceify.contract.client.ProjectClient;
import com.ga.lanceify.contract.client.ProposalClient;
import com.ga.lanceify.contract.dto.ProposalSummary;
import com.ga.lanceify.contract.model.Contract;
import com.ga.lanceify.contract.model.ContractStatus;
import com.ga.lanceify.contract.repository.ContractRepository;

@Service
public class ContractService {

	public static final int MAX_ACTIVE_CONTRACTS = 3;

	private final ContractRepository repository;

	private final ProposalClient proposals;

	private final ProjectClient projects;

	private final Map<String, ReentrantLock> freelancerLocks = new ConcurrentHashMap<>();

	public ContractService(ContractRepository repository, ProposalClient proposals, ProjectClient projects) {
		this.repository = repository;
		this.proposals = proposals;
		this.projects = projects;
	}

	public Contract createFromProposal(String proposalId, String userId, boolean admin) {
		ProposalSummary proposal = proposals.get(proposalId);
		if (!admin && !userId.equals(proposal.clientId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found");
		}
		if (!"ACCEPTED".equals(proposal.status())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an accepted proposal can become a contract");
		}

		ReentrantLock lock = freelancerLocks.computeIfAbsent(proposal.freelancerId(), id -> new ReentrantLock());
		lock.lock();
		try {
			return repository.findByProposalId(proposalId).orElseGet(() -> insert(proposal));
		} finally {
			lock.unlock();
		}
	}

	private Contract insert(ProposalSummary proposal) {
		long active = repository.countByFreelancerIdAndStatus(proposal.freelancerId(), ContractStatus.ACTIVE);
		if (active >= MAX_ACTIVE_CONTRACTS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"This freelancer already has " + MAX_ACTIVE_CONTRACTS + " active contracts");
		}
		Contract contract = new Contract();
		contract.setProposalId(proposal.id());
		contract.setProjectId(proposal.projectId());
		contract.setProjectTitle(proposal.projectTitle());
		contract.setClientId(proposal.clientId());
		contract.setFreelancerId(proposal.freelancerId());
		contract.setAmount(proposal.bidAmount());
		contract.setDeliveryDays(proposal.deliveryDays());
		contract.setCreatedAt(Instant.now());
		return repository.insert(contract);
	}

	public List<Contract> forUser(String userId, boolean client) {
		return client
				? repository.findByClientIdOrderByCreatedAtDesc(userId)
				: repository.findByFreelancerIdOrderByCreatedAtDesc(userId);
	}

	public List<Contract> all() {
		return repository.findAll();
	}

	public Contract get(String id, String userId, boolean admin) {
		Contract contract = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
		if (!admin && !userId.equals(contract.getClientId()) && !userId.equals(contract.getFreelancerId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found");
		}
		return contract;
	}

	public Contract complete(String id, String clientId) {
		Contract contract = getActive(id, clientId, false);
		if (!clientId.equals(contract.getClientId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the client can complete this contract");
		}
		updateProject(contract, "COMPLETED");
		return close(contract, ContractStatus.COMPLETED);
	}

	public Contract cancel(String id, String userId, boolean admin, boolean updateProject) {
		Contract contract = getActive(id, userId, admin);
		if (!admin && !userId.equals(contract.getClientId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the client can cancel this contract");
		}
		if (updateProject) {
			updateProject(contract, "CANCELLED");
		}
		return close(contract, ContractStatus.CANCELLED);
	}

	private Contract getActive(String id, String userId, boolean admin) {
		Contract contract = get(id, userId, admin);
		if (contract.getStatus() != ContractStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This contract is already closed");
		}
		return contract;
	}

	private void updateProject(Contract contract, String status) {
		try {
			projects.changeStatus(contract.getProjectId(), status);
		} catch (RestClientException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The project could not be updated");
		}
	}

	private Contract close(Contract contract, ContractStatus status) {
		contract.setStatus(status);
		contract.setClosedAt(Instant.now());
		return repository.save(contract);
	}

}
