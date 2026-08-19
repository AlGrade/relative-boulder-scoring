package com.boulderscoring.competitor;

public class NameAlreadyTakenException extends RuntimeException {

	public NameAlreadyTakenException(String name) {
		super("Der Name \"%s\" ist bereits vergeben.".formatted(name));
	}
}
