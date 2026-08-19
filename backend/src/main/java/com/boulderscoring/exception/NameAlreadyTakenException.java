package com.boulderscoring.exception;

public class NameAlreadyTakenException extends RuntimeException {

	public NameAlreadyTakenException(String name) {
		super("Der Name \"%s\" ist bereits vergeben.".formatted(name));
	}
}
