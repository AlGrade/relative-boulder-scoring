package com.boulderscoring.boulder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/**
 * Ein Boulder der laufenden Runde. Boulder werden direkt per SQL angelegt
 * (siehe README) — die Anwendung liest sie nur.
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
		// fuer Hibernate
	}
}
