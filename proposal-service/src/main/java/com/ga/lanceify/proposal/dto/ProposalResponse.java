package com.ga.lanceify.proposal.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.ga.lanceify.proposal.model.Proposal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {

    private String id;

    private String projectId;

    private String clientId;

    private String projectTitle;

    private String freelancerId;

    private String coverLetter;

    private BigDecimal bidAmount;

    private Integer deliveryDays;

    private Proposal.ProposalStatus status;

    private Instant createdAt;

    private Instant decidedAt;

    private Long version;

    public static ProposalResponse from(Proposal proposal) {
        return new ProposalResponse(
                proposal.getId(),
                proposal.getProjectId(),
                proposal.getClientId(),
                proposal.getProjectTitle(),
                proposal.getFreelancerId(),
                proposal.getCoverLetter(),
                proposal.getBidAmount(),
                proposal.getDeliveryDays(),
                proposal.getStatus(),
                proposal.getCreatedAt(),
                proposal.getDecidedAt(),
                proposal.getVersion());
    }

}
