package com.boulderscoring.competitor;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Traegt die Competitor-ID mit in den Security-Context, damit Controller ueber
 * {@code @AuthenticationPrincipal} ohne zusaetzliche Query an sie herankommen.
 */
public record CompetitorPrincipal(Long id, String name, Gender gender, String passwordHash) implements UserDetails {

	static CompetitorPrincipal of(Competitor competitor) {
		return new CompetitorPrincipal(competitor.getId(), competitor.getName(), competitor.getGender(),
				competitor.getPasswordHash());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return name;
	}
}
