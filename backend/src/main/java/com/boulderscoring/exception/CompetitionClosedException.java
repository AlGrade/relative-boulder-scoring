package com.boulderscoring.exception;

public class CompetitionClosedException extends RuntimeException {

	public CompetitionClosedException() {
		super("Der Wettkampf läuft gerade nicht.");
	}
}
