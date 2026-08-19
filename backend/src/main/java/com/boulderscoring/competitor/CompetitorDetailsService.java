package com.boulderscoring.competitor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class CompetitorDetailsService implements UserDetailsService {

	private final CompetitorRepository competitors;

	CompetitorDetailsService(CompetitorRepository competitors) {
		this.competitors = competitors;
	}

	@Override
	public UserDetails loadUserByUsername(String name) {
		return competitors.findByNameIgnoreCase(name)
			.map(CompetitorPrincipal::of)
			.orElseThrow(() -> new UsernameNotFoundException("Unbekannter Teilnehmer: " + name));
	}
}
