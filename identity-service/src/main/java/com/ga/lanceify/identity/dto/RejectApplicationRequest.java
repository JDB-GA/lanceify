package com.ga.lanceify.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectApplicationRequest(@NotBlank @Size(max = 500) String reason) {
}
