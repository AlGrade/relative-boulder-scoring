package com.boulderscoring.exception;

public class BoulderNotFoundException extends RuntimeException {

	public BoulderNotFoundException(int number) {
		super("Boulder %d gibt es nicht.".formatted(number));
	}
}
