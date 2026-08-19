package com.boulderscoring.competitor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CompetitorService {

	private final CompetitorRepository competitors;
	private final PasswordEncoder passwordEncoder;

	CompetitorService(CompetitorRepository competitors, PasswordEncoder passwordEncoder) {
		this.competitors = competitors;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * The upfront check covers the normal case; the unique index on {@code name} is
	 * still needed - it decides when two registrations race each other.
	 */
	@Transactional
	Competitor register(String name, Gender gender, String rawPassword) {
		String trimmedName = name.trim();
		if (competitors.existsByNameIgnoreCase(trimmedName)) {
			throw new NameAlreadyTakenException(trimmedName);
		}
		return competitors.save(new Competitor(trimmedName, gender, passwordEncoder.encode(rawPassword)));
	}
}
