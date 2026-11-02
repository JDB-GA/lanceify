package com.ga.lanceify.contract.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateContractRequest(@NotBlank String proposalId) {
}
