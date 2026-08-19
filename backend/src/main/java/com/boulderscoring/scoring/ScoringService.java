package com.boulderscoring.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.boulderscoring.ascent.Ascent;
import com.boulderscoring.ascent.AscentRepository;
import com.boulderscoring.boulder.Boulder;
import com.boulderscoring.boulder.BoulderRepository;
import com.boulderscoring.competitor.CompetitorRepository;
import com.boulderscoring.competitor.Gender;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die relative Wertung: ein Boulder ist umso mehr wert, je weniger Leute ihn geschafft
 * haben. Gerechnet wird je Wertungsklasse getrennt, ein Boulder kann also fuer Frauen
 * und Maenner unterschiedlich viel zaehlen.
 */
@Service
class ScoringService {

	private static final double POINTS_PER_BOULDER = 1000d;

	private static final double FLASH_FACTOR = 1.2d;

	private final AscentRepository ascents;
	private final BoulderRepository boulders;
	private final CompetitorRepository competitors;

	ScoringService(AscentRepository ascents, BoulderRepository boulders, CompetitorRepository competitors) {
		this.ascents = ascents;
		this.boulders = boulders;
		this.competitors = competitors;
	}

	/** Was jeder Boulder aktuell wert ist, der wertvollste zuerst. */
	@Transactional(readOnly = true)
	List<BoulderPoints> boulderPoints(Gender gender) {
		return snapshot(gender).pointsByBoulder()
			.entrySet()
			.stream()
			.map(entry -> new BoulderPoints(entry.getKey(), round(entry.getValue())))
			.sorted(Comparator.comparingDouble(BoulderPoints::points)
				.reversed()
				.thenComparingInt(BoulderPoints::boulderNumber))
			.toList();
	}

	/** Rangliste einer Wertungsklasse. Wer nichts geschafft hat, steht mit 0 Punkten drin. */
	@Transactional(readOnly = true)
	List<RankingEntry> ranking(Gender gender) {
		Snapshot snapshot = snapshot(gender);

		Map<Long, Double> totals = new HashMap<>();
		for (Ascent ascent : snapshot.ascents()) {
			double points = snapshot.pointsByBoulder().getOrDefault(ascent.getBoulder().getNumber(), 0d);
			if (ascent.isFlashed()) {
				points *= FLASH_FACTOR;
			}
			totals.merge(ascent.getCompetitor().getId(), points, Double::sum);
		}

		List<RankingEntry> sorted = competitors.findAllByGender(gender)
			.stream()
			.map(competitor -> new RankingEntry(0, competitor.getName(),
					round(totals.getOrDefault(competitor.getId(), 0d))))
			.sorted(Comparator.comparingDouble(RankingEntry::points).reversed().thenComparing(RankingEntry::name))
			.toList();

		return withRanks(sorted);
	}

	/**
	 * Ein Durchlauf ueber die Wertungsklasse: alle Begehungen plus der daraus abgeleitete
	 * Wert jedes Boulders. Boulder, die niemand geschafft hat, sind volle
	 * {@value #POINTS_PER_BOULDER} Punkte wert.
	 */
	private Snapshot snapshot(Gender gender) {
		List<Ascent> genderAscents = ascents.findAllByCompetitorGender(gender);

		Map<Integer, Long> ascentCounts = genderAscents.stream()
			.collect(Collectors.groupingBy(ascent -> ascent.getBoulder().getNumber(), Collectors.counting()));

		Map<Integer, Double> pointsByBoulder = boulders.findAllByOrderByNumberAsc()
			.stream()
			.collect(Collectors.toMap(Boulder::getNumber,
					boulder -> pointsFor(ascentCounts.getOrDefault(boulder.getNumber(), 0L))));

		return new Snapshot(genderAscents, pointsByBoulder);
	}

	private static double pointsFor(long ascentCount) {
		return ascentCount == 0 ? POINTS_PER_BOULDER : POINTS_PER_BOULDER / ascentCount;
	}

	/** Gleiche Punkte bedeuten gleichen Rang, der naechste Rang ueberspringt entsprechend. */
	private static List<RankingEntry> withRanks(List<RankingEntry> sortedByPoints) {
		List<RankingEntry> ranked = new ArrayList<>(sortedByPoints.size());
		double previousPoints = Double.NaN;
		int rank = 0;

		for (int i = 0; i < sortedByPoints.size(); i++) {
			RankingEntry entry = sortedByPoints.get(i);
			if (entry.points() != previousPoints) {
				rank = i + 1;
				previousPoints = entry.points();
			}
			ranked.add(new RankingEntry(rank, entry.name(), entry.points()));
		}
		return ranked;
	}

	private static double round(double points) {
		return Math.round(points * 100d) / 100d;
	}

	private record Snapshot(List<Ascent> ascents, Map<Integer, Double> pointsByBoulder) {
	}
}
