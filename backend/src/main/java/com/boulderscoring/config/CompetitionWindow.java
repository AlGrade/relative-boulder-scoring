package com.boulderscoring.config;

import java.time.Instant;
import java.util.Optional;

import com.boulderscoring.exception.CompetitionClosedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Until when the competition accepts changes. Registrations and ascents are refused
 * once the deadline has passed; reading the ranking stays public either way.
 *
 * <p>A deadline rather than an on/off switch, because the two mistakes are not equally
 * bad: forgetting to open the window means nobody can register and someone says so
 * within a minute, while forgetting to close it lets anyone who finds the URL keep
 * adding competitors for the rest of the year. A deadline can only fail the loud way.
 *
 * <p>No value configured means closed - the app is only ever open on purpose.
 */
@Component
public class CompetitionWindow {

	private final Optional<Instant> openUntil;

	CompetitionWindow(@Value("${comp.open-until:}") String openUntil) {
		this.openUntil = openUntil.isBlank() ? Optional.empty() : Optional.of(Instant.parse(openUntil.trim()));
	}

	public boolean isOpen() {
		return this.openUntil.filter(Instant.now()::isBefore).isPresent();
	}

	public void requireOpen() {
		if (!isOpen()) {
			throw new CompetitionClosedException();
		}
	}
}
