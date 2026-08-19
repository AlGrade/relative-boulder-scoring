package com.boulderscoring.competitor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final CompetitorService competitors;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	AuthController(CompetitorService competitors, AuthenticationManager authenticationManager) {
		this.competitors = competitors;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	CompetitorResponse register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		Competitor competitor = competitors.register(body.name(), body.gender(), body.password());
		// Direkt einloggen — wer sich registriert, will nicht gleich ein Login-Formular sehen.
		startSession(competitor.getName(), body.password(), request, response);
		return new CompetitorResponse(competitor.getId(), competitor.getName(), competitor.getGender());
	}

	@PostMapping("/login")
	CompetitorResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		return CompetitorResponse.of(startSession(body.name(), body.password(), request, response));
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void logout(HttpServletRequest request, HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
	}

	@GetMapping("/me")
	CompetitorResponse me(@AuthenticationPrincipal CompetitorPrincipal principal) {
		return CompetitorResponse.of(principal);
	}

	/**
	 * Login von Hand statt ueber {@code formLogin} — das erwartet Form-Encoding, wir
	 * sprechen JSON. Die Session-ID wechselt dabei bewusst (Session Fixation).
	 */
	private CompetitorPrincipal startSession(String name, String password, HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager
			.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(name, password));

		HttpSession existingSession = request.getSession(false);
		if (existingSession != null) {
			request.changeSessionId();
		}

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);

		return (CompetitorPrincipal) authentication.getPrincipal();
	}
}
