package com.ga.lanceify.proposal.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ga.lanceify.proposal.dto.ProposalResponse;
import com.ga.lanceify.proposal.security.CurrentUser;
import com.ga.lanceify.proposal.service.ProposalService;

@PreAuthorize("hasRole('CLIENT')")
@RestController
public class ClientProposalController {

    private final ProposalService service;
    private final CurrentUser currentUser;

    public ClientProposalController(ProposalService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/proposals/received")
    public List<ProposalResponse> received() {
        return service.receivedProposals(currentUser.getUserId()).stream().map(ProposalResponse::from).toList();
    }

    @GetMapping("/api/projects/{projectId}/proposals")
    public List<ProposalResponse> forProject(@PathVariable String projectId) {
        return service.projectProposals(currentUser.getUserId(), projectId).stream().map(ProposalResponse::from).toList();
    }

    @PutMapping("/api/proposals/{id}/accept")
    public ProposalResponse accept(@PathVariable String id, @RequestParam(required = false) Long version) {
        return ProposalResponse.from(service.accept(currentUser.getUserId(), id, version));
    }

    @PutMapping("/api/proposals/{id}/reject")
    public ProposalResponse reject(@PathVariable String id, @RequestParam(required = false) Long version) {
        return ProposalResponse.from(service.reject(currentUser.getUserId(), id, version));
    }

}
