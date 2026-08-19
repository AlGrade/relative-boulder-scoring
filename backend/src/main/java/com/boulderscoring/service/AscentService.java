package com.boulderscoring.service;

import java.util.List;

import com.boulderscoring.dto.AscentResponse;
import com.boulderscoring.exception.BoulderNotFoundException;
import com.boulderscoring.model.Ascent;
import com.boulderscoring.model.Boulder;
import com.boulderscoring.repository.AscentRepository;
import com.boulderscoring.repository.BoulderRepository;
import com.boulderscoring.repository.CompetitorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AscentService {

	private final AscentRepository ascents;
	private final BoulderRepository boulders;
	private final CompetitorRepository competitors;

	AscentService(AscentRepository ascents, BoulderRepository boulders, CompetitorRepository competitors) {
		this.ascents = ascents;
		this.boulders = boulders;
		this.competitors = competitors;
	}

	@Transactional(readOnly = true)
	public List<AscentResponse> findFor(Long competitorId) {
		return ascents.findAllByCompetitorIdOrderByBoulderNumberAsc(competitorId)
			.stream()
			.map(AscentResponse::of)
			.toList();
	}

	/** Creates the ascent if it does not exist yet and sets the flash flag. */
	@Transactional
	public void record(Long competitorId, int boulderNumber, boolean flashed) {
		Ascent ascent = ascents.findByCompetitorIdAndBoulderNumber(competitorId, boulderNumber)
			.orElseGet(() -> new Ascent(competitors.getReferenceById(competitorId), boulder(boulderNumber)));
		ascent.setFlashed(flashed);
		ascents.save(ascent);
	}

	/** Removes the ascent along with its flash. */
	@Transactional
	public void remove(Long competitorId, int boulderNumber) {
		ascents.deleteByCompetitorIdAndBoulderNumber(competitorId, boulderNumber);
	}

	private Boulder boulder(int number) {
		return boulders.findByNumber(number).orElseThrow(() -> new BoulderNotFoundException(number));
	}
}
