package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import java.text.ParseException;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * WebSecurityConfig class
 *
 * @author Karl Nicholas
 */
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {
	private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	@Value("${app.public_routes}")
	private String[] publicRoutes;
	private final JwtProperties jwtProperties;

	public WebSecurityConfig(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authManager) {
		return http
			.authorizeExchange()
				.pathMatchers(HttpMethod.OPTIONS)
					.permitAll()
				.pathMatchers(publicRoutes)
					.permitAll()
				.pathMatchers("/favicon.ico")
					.permitAll()
				.anyExchange()
					.authenticated()
				.and()
			.csrf().disable()
			.httpBasic().disable()
			.formLogin().disable()
			.logout(logout -> logout.requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout")))
			.exceptionHandling().authenticationEntryPoint((swe, e) -> {
				logger.info("[1] Authentication error: Unauthorized[401]: " + e.getMessage());
				return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
			}).accessDeniedHandler((swe, e) -> {
				logger.info("[2] Authentication error: Access Denied[401]: " + e.getMessage());
				return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
			})
			.and()
			.addFilterAt(createAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfiguration() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.applyPermitDefaultValues();
		corsConfig.addAllowedOrigin("http://localhost:3000");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

	AuthenticationWebFilter createAuthenticationFilter(ReactiveAuthenticationManager authManager) {
		AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(authManager);
		authenticationFilter.setServerAuthenticationConverter(exchange -> {
			return Mono.justOrEmpty(getBearerToken(exchange).map(token -> {
				return jwtProperties.verifyToken(token).map(signedJWT -> {
					try {
						com.nimbusds.jose.shaded.json.JSONArray jsonArray = (com.nimbusds.jose.shaded.json.JSONArray) signedJWT.getJWTClaimsSet().getClaim("role");
						SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority((String) jsonArray.get(0));
						return new UsernamePasswordAuthenticationToken(signedJWT.getJWTClaimsSet().getSubject(), null, Collections.singletonList(simpleGrantedAuthority));
					} catch (ParseException e) {
						e.printStackTrace();
						return null;
					}
				}).orElseGet(() -> null);
			}).orElseGet(() -> null));
		});
		authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
		return authenticationFilter;
	}

	@Bean
	public ReactiveAuthenticationManager authenticationManager() {
		return Mono::just;
	}

	private static final String BEARER = "Bearer ";

	public static Optional<String> getBearerToken(ServerWebExchange serverWebExchange) {
		String token = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (token == null)
			return Optional.empty();
		if (token.length() <= BEARER.length())
			return Optional.empty();
		return Optional.of(token.substring(BEARER.length()));
	}

}
