package com.boulderscoring.boulder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/**
 * A boulder of the current round. Boulders are created directly via SQL (see the
 * README) - the application only reads them.
 */
@Entity
@Getter
public class Boulder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private int number;

	protected Boulder() {
		// for Hibernate
	}
}
