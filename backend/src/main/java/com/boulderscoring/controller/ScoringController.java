package com.boulderscoring.controller;

import java.util.List;

import com.boulderscoring.dto.BoulderPoints;
import com.boulderscoring.dto.RankingEntry;
import com.boulderscoring.model.Gender;
import com.boulderscoring.service.ScoringService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class ScoringController {

	private final ScoringService scoring;

	ScoringController(ScoringService scoring) {
		this.scoring = scoring;
	}

	@GetMapping("/ranking")
	List<RankingEntry> ranking(@RequestParam Gender gender) {
		return scoring.ranking(gender);
	}

	@GetMapping("/boulder-points")
	List<BoulderPoints> boulderPoints(@RequestParam Gender gender) {
		return scoring.boulderPoints(gender);
	}
}
