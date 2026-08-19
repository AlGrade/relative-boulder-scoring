package com.boulderscoring.boulder;

import java.util.List;

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

record BoulderResponse(Long id, int number) {

	static BoulderResponse of(Boulder boulder) {
		return new BoulderResponse(boulder.getId(), boulder.getNumber());
	}
}
