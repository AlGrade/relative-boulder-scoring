package com.boulderscoring.dto;

import com.boulderscoring.model.Boulder;

public record BoulderResponse(Long id, int number) {

	public static BoulderResponse of(Boulder boulder) {
		return new BoulderResponse(boulder.getId(), boulder.getNumber());
	}
}
