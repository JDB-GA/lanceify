package com.ga.lanceify.proposal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRequest {

    @NotBlank(message = "Cover letter is required")
    @Size(min = 20, max = 2000, message = "Cover letter must be 20-2000 characters")
    private String coverLetter;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "1.0", message = "Bid amount must be at least 1")
    private BigDecimal bidAmount;

    @NotNull(message = "Delivery days is required")
    @Min(value = 1, message = "Delivery days must be at least 1")
    @Max(value = 365, message = "Delivery days must be at most 365")
    private Integer deliveryDays;

}
