package com.boulderscoring.controller;

import com.boulderscoring.config.CompetitionWindow;
import com.boulderscoring.dto.CompetitionResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, so the landing page knows whether to offer a registration at all instead of
 * finding out from a rejected request.
 */
@RestController
@RequestMapping("/api/competition")
class CompetitionController {

	private final CompetitionWindow window;

	CompetitionController(CompetitionWindow window) {
		this.window = window;
	}

	@GetMapping
	CompetitionResponse state() {
		return new CompetitionResponse(this.window.isOpen());
	}
}
