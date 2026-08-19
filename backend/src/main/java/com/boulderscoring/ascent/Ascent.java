package com.boulderscoring.ascent;

import com.boulderscoring.boulder.Boulder;
import com.boulderscoring.competitor.Competitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Eine Begehung: dieser Teilnehmer hat diesen Boulder geschafft. Ein Flash ist ein
 * Flag darauf — dadurch ist ein Flash ohne Begehung strukturell unmoeglich.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "competitor_id", "boulder_id" }))
@Getter
public class Ascent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "competitor_id", nullable = false)
	private Competitor competitor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "boulder_id", nullable = false)
	private Boulder boulder;

	@Setter
	@Column(nullable = false)
	private boolean flashed;

	protected Ascent() {
		// fuer Hibernate
	}

	Ascent(Competitor competitor, Boulder boulder) {
		this.competitor = competitor;
		this.boulder = boulder;
	}
}
