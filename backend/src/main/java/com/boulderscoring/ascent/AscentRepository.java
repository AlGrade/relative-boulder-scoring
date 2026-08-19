package com.boulderscoring.ascent;

import java.util.List;
import java.util.Optional;

import com.boulderscoring.competitor.Gender;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AscentRepository extends JpaRepository<Ascent, Long> {

	/** Basis of the scoring: every ascent of one scoring class in a single query. */
	@EntityGraph(attributePaths = { "competitor", "boulder" })
	List<Ascent> findAllByCompetitorGender(Gender gender);

	@EntityGraph(attributePaths = "boulder")
	List<Ascent> findAllByCompetitorIdOrderByBoulderNumberAsc(Long competitorId);

	Optional<Ascent> findByCompetitorIdAndBoulderNumber(Long competitorId, int boulderNumber);

	void deleteByCompetitorIdAndBoulderNumber(Long competitorId, int boulderNumber);
}
