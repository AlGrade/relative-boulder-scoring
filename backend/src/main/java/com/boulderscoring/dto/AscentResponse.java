package com.boulderscoring.dto;

import com.boulderscoring.model.Ascent;

public record AscentResponse(int boulderNumber, boolean flashed) {

	public static AscentResponse of(Ascent ascent) {
		return new AscentResponse(ascent.getBoulder().getNumber(), ascent.isFlashed());
	}
}
