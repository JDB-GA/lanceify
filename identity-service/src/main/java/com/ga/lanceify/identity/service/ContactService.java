package com.ga.lanceify.identity.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ga.lanceify.identity.dto.ContactResponse;
import com.ga.lanceify.identity.model.ApprovalStatus;
import com.ga.lanceify.identity.model.FreelancerProfile;
import com.ga.lanceify.identity.model.User;
import com.ga.lanceify.identity.repository.FreelancerProfileRepository;
import com.ga.lanceify.identity.repository.UserRepository;

@Service
public class ContactService {

	private final UserRepository userRepository;

	private final FreelancerProfileRepository profileRepository;

	public ContactService(UserRepository userRepository, FreelancerProfileRepository profileRepository) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
	}

	public List<ContactResponse> clients() {
		return userRepository.findByRoleAndBannedFalse(User.Role.CLIENT).stream()
				.map(ContactResponse::from)
				.toList();
	}

	public List<ContactResponse> approvedFreelancers() {
		Set<String> approvedIds = profileRepository.findAllByApprovalStatus(ApprovalStatus.APPROVED).stream()
				.map(FreelancerProfile::getUserId)
				.collect(Collectors.toSet());
		return userRepository.findByRoleAndBannedFalse(User.Role.FREELANCER).stream()
				.filter(user -> approvedIds.contains(user.getId()))
				.map(ContactResponse::from)
				.toList();
	}

}
