package com.ga.lanceify.contract.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProposalSummary(String id, String projectId, String projectTitle, String clientId, String freelancerId,
		BigDecimal bidAmount, Integer deliveryDays, String status) {
}
