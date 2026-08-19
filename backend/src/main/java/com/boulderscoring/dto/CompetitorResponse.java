package com.boulderscoring.dto;

import com.boulderscoring.model.Gender;
import com.boulderscoring.security.CompetitorPrincipal;

public record CompetitorResponse(Long id, String name, Gender gender) {

	public static CompetitorResponse of(CompetitorPrincipal principal) {
		return new CompetitorResponse(principal.id(), principal.name(), principal.gender());
	}
}
