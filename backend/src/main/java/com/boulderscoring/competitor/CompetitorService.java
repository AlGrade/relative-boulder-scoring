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
	 * Der Vorab-Check faengt den Normalfall ab, den Unique-Index auf {@code name}
	 * braucht es trotzdem — er entscheidet bei zwei gleichzeitigen Registrierungen.
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
