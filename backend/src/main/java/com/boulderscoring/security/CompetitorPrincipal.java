package com.boulderscoring.security;

import java.util.Collection;
import java.util.List;

import com.boulderscoring.model.Competitor;
import com.boulderscoring.model.Gender;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Carries the competitor id into the security context so controllers can reach it
 * through {@code @AuthenticationPrincipal} without an extra query.
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
