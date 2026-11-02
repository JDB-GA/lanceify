package com.ga.lanceify.contract.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ga.lanceify.contract.model.Contract;
import com.ga.lanceify.contract.model.ContractStatus;

public interface ContractRepository extends MongoRepository<Contract, String> {

	Optional<Contract> findByProposalId(String proposalId);

	long countByFreelancerIdAndStatus(String freelancerId, ContractStatus status);

	List<Contract> findByClientIdOrderByCreatedAtDesc(String clientId);

	List<Contract> findByFreelancerIdOrderByCreatedAtDesc(String freelancerId);

}
