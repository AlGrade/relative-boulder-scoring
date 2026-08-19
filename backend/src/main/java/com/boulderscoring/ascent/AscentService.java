package com.boulderscoring.ascent;

import java.util.List;

import com.boulderscoring.boulder.Boulder;
import com.boulderscoring.boulder.BoulderNotFoundException;
import com.boulderscoring.boulder.BoulderRepository;
import com.boulderscoring.competitor.CompetitorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AscentService {

	private final AscentRepository ascents;
	private final BoulderRepository boulders;
	private final CompetitorRepository competitors;

	AscentService(AscentRepository ascents, BoulderRepository boulders, CompetitorRepository competitors) {
		this.ascents = ascents;
		this.boulders = boulders;
		this.competitors = competitors;
	}

	@Transactional(readOnly = true)
	List<AscentResponse> findFor(Long competitorId) {
		return ascents.findAllByCompetitorIdOrderByBoulderNumberAsc(competitorId)
			.stream()
			.map(AscentResponse::of)
			.toList();
	}

	/** Legt die Begehung an, falls es sie noch nicht gibt, und setzt das Flash-Flag. */
	@Transactional
	void record(Long competitorId, int boulderNumber, boolean flashed) {
		Ascent ascent = ascents.findByCompetitorIdAndBoulderNumber(competitorId, boulderNumber)
			.orElseGet(() -> new Ascent(competitors.getReferenceById(competitorId), boulder(boulderNumber)));
		ascent.setFlashed(flashed);
		ascents.save(ascent);
	}

	/** Entfernt die Begehung samt Flash. */
	@Transactional
	void remove(Long competitorId, int boulderNumber) {
		ascents.deleteByCompetitorIdAndBoulderNumber(competitorId, boulderNumber);
	}

	private Boulder boulder(int number) {
		return boulders.findByNumber(number).orElseThrow(() -> new BoulderNotFoundException(number));
	}
}
