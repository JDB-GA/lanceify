package com.ga.lanceify.proposal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.proposal.dto.ApplyRequest;
import com.ga.lanceify.proposal.dto.ProposalResponse;
import com.ga.lanceify.proposal.security.CurrentUser;
import com.ga.lanceify.proposal.service.ProposalService;

import jakarta.validation.Valid;

@PreAuthorize("hasRole('FREELANCER')")
@RestController
public class FreelancerProposalController {

    private final ProposalService service;
    private final CurrentUser currentUser;

    public FreelancerProposalController(ProposalService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping("/api/projects/{projectId}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse apply(@PathVariable String projectId, @Valid @RequestBody ApplyRequest request) {
        return ProposalResponse.from(service.apply(currentUser.getUserId(), projectId, request));
    }

    @GetMapping("/api/proposals/me")
    public List<ProposalResponse> myProposals() {
        return service.myProposals(currentUser.getUserId()).stream().map(ProposalResponse::from).toList();
    }

    @PutMapping("/api/proposals/{id}/withdraw")
    public ProposalResponse withdraw(@PathVariable String id) {
        return ProposalResponse.from(service.withdraw(currentUser.getUserId(), id));
    }

}
