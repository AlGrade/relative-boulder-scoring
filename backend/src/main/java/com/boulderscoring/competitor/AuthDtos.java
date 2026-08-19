package com.boulderscoring.competitor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record RegisterRequest(
		@NotBlank @Size(max = 100) String name,
		@NotNull Gender gender,
		@NotBlank @Size(min = 6, max = 100) String password) {
}

record LoginRequest(
		@NotBlank String name,
		@NotBlank String password) {
}

record CompetitorResponse(Long id, String name, Gender gender) {

	static CompetitorResponse of(CompetitorPrincipal principal) {
		return new CompetitorResponse(principal.id(), principal.name(), principal.gender());
	}
}
