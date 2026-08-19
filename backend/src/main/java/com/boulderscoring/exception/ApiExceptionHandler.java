package com.boulderscoring.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Everything else (bean validation, unknown paths) is answered by Spring itself as a
 * ProblemDetail, see {@code spring.mvc.problemdetails.enabled}.
 */
@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler({ NameAlreadyTakenException.class, DataIntegrityViolationException.class })
	ProblemDetail nameAlreadyTaken(RuntimeException exception) {
		String detail = exception instanceof NameAlreadyTakenException ? exception.getMessage()
				: "Der Name ist bereits vergeben.";
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail badCredentials(AuthenticationException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Name oder Passwort ist nicht korrekt.");
	}

	@ExceptionHandler(BoulderNotFoundException.class)
	ProblemDetail boulderNotFound(BoulderNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(CompetitionClosedException.class)
	ProblemDetail competitionClosed(CompetitionClosedException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
	}
}
