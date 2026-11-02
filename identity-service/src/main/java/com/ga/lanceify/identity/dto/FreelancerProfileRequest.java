package com.ga.lanceify.identity.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FreelancerProfileRequest(
		@Size(max = 2000) String bio,
		@PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal minPrice,
		@PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal maxPrice,
		@Size(max = 15) List<@NotBlank String> skillIds) {

	@AssertTrue(message = "minPrice must not be greater than maxPrice")
	public boolean isPriceRangeValid() {
		return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
	}

}
