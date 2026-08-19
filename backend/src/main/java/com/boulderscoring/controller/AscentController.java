package com.boulderscoring.controller;

import java.util.List;

import com.boulderscoring.dto.AscentRequest;
import com.boulderscoring.dto.AscentResponse;
import com.boulderscoring.security.CompetitorPrincipal;
import com.boulderscoring.service.AscentService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/ascents")
class AscentController {

	private final AscentService ascents;

	AscentController(AscentService ascents) {
		this.ascents = ascents;
	}

	@GetMapping
	List<AscentResponse> mine(@AuthenticationPrincipal CompetitorPrincipal me) {
		return ascents.findFor(me.id());
	}

	@PutMapping("/{boulderNumber}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void record(@AuthenticationPrincipal CompetitorPrincipal me, @PathVariable int boulderNumber,
			@RequestBody AscentRequest body) {
		ascents.record(me.id(), boulderNumber, body.flashed());
	}

	@DeleteMapping("/{boulderNumber}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void remove(@AuthenticationPrincipal CompetitorPrincipal me, @PathVariable int boulderNumber) {
		ascents.remove(me.id(), boulderNumber);
	}
}
