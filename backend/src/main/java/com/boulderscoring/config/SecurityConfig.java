package com.boulderscoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			.csrfTokenRequestHandler(spaCsrfTokenRequestHandler()))
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login")
				.permitAll()
				// Rangliste und Boulderwerte sind oeffentlich — die Landing Page zeigt
				// sie auch ohne Login.
				.requestMatchers(HttpMethod.GET, "/api/boulders", "/api/ranking", "/api/boulder-points")
				.permitAll()
				.requestMatchers("/actuator/health")
				.permitAll()
				.anyRequest()
				.authenticated())
			// Ohne Login gibt es 401 statt eines Redirects auf ein Login-Formular,
			// das es hier nicht gibt.
			.exceptionHandling(handling -> handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.build();
	}

	/**
	 * Der Default-Handler verschluesselt den CSRF-Token pro Request (BREACH-Schutz) und
	 * ist damit inkompatibel damit, dass Angular den rohen Cookie-Wert als
	 * {@code X-XSRF-TOKEN} zurueckschickt. Das Nullen des Request-Attributnamens laedt
	 * den Token ausserdem bei jedem Request, sodass das Cookie schon vor dem ersten
	 * schreibenden Aufruf gesetzt ist.
	 */
	private static CsrfTokenRequestAttributeHandler spaCsrfTokenRequestHandler() {
		CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
		handler.setCsrfRequestAttributeName(null);
		return handler;
	}
}
