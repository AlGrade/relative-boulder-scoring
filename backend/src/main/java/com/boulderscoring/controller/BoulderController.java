package com.boulderscoring.controller;

import java.util.List;

import com.boulderscoring.dto.BoulderResponse;
import com.boulderscoring.repository.BoulderRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boulders")
class BoulderController {

	private final BoulderRepository boulders;

	BoulderController(BoulderRepository boulders) {
		this.boulders = boulders;
	}

	@GetMapping
	List<BoulderResponse> all() {
		return boulders.findAllByOrderByNumberAsc().stream().map(BoulderResponse::of).toList();
	}
}
