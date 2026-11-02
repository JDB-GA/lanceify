package com.ga.lanceify.proposal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContractSummary(String id, String status) {
}
