package com.boulderscoring.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.boulderscoring.dto.BoulderPoints;
import com.boulderscoring.dto.RankingEntry;
import com.boulderscoring.model.Ascent;
import com.boulderscoring.model.Boulder;
import com.boulderscoring.model.Gender;
import com.boulderscoring.repository.AscentRepository;
import com.boulderscoring.repository.BoulderRepository;
import com.boulderscoring.repository.CompetitorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The relative scoring: a boulder is worth more the fewer people have sent it.
 * Calculated per scoring class, so the same boulder can be worth different amounts
 * for women and men.
 */
@Service
public class ScoringService {

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

	/** What each boulder is currently worth, most valuable first. */
	@Transactional(readOnly = true)
	public List<BoulderPoints> boulderPoints(Gender gender) {
		return snapshot(gender).pointsByBoulder()
			.entrySet()
			.stream()
			.map(entry -> new BoulderPoints(entry.getKey(), round(entry.getValue())))
			.sorted(Comparator.comparingDouble(BoulderPoints::points)
				.reversed()
				.thenComparingInt(BoulderPoints::boulderNumber))
			.toList();
	}

	/** Ranking of one scoring class. Whoever sent nothing is listed with 0 points. */
	@Transactional(readOnly = true)
	public List<RankingEntry> ranking(Gender gender) {
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
	 * One pass over a scoring class: every ascent plus the boulder values derived from
	 * them. Boulders nobody has sent are worth the full
	 * {@value #POINTS_PER_BOULDER} points.
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

	/**
	 * Equal points mean equal rank; the next rank skips accordingly. Compares the
	 * rounded totals - see {@link #round} for why that comparison is exact.
	 */
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

	/**
	 * One decimal, and deliberately applied before anything compares two totals.
	 *
	 * <p>Summing the same boulders in a different order does not give the same double:
	 * the values are mostly repeating decimals, so two people who climbed exactly the
	 * same problems can end up roughly 1e-12 apart and miss a tie they have earned.
	 * Rounding first makes that difference disappear.
	 *
	 * <p>Going through {@link Math#round} is what makes the equality check in
	 * {@link #withRanks} sound: two totals that round to the same tenth are divided
	 * from the same long by the same divisor, so they come out bit-identical rather
	 * than merely close.
	 */
	private static double round(double points) {
		return Math.round(points * 10d) / 10d;
	}

	private record Snapshot(List<Ascent> ascents, Map<Integer, Double> pointsByBoulder) {
	}
}
