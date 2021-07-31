package com.github.karlnicholas.webfluxjwtsecurity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.configuration.JwtProperties;
import com.github.karlnicholas.webfluxjwtsecurity.dto.AuthResultDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.UserLoginDto;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

import java.util.*;
import javax.security.auth.login.FailedLoginException;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.*;

/**
 * SecurityService class
 *
 * @author Karl Nicholas
 */
@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProperties jwtProperties;

	public AuthService(JwtProperties jwtProperties, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.jwtProperties = jwtProperties;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	private AuthResultDto generateAccessToken(User user) {
		long expirationTimeInMilliseconds = jwtProperties.getExpiration() * 1000;
		Date expirationDate = new Date(new Date().getTime() + expirationTimeInMilliseconds);
		Date createdDate = new Date();

		try {

			// Prepare JWT with claims set
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
					.claim("role", user.getRoles())
					.subject(user.getUsername())
					.issueTime(createdDate)
					.expirationTime(expirationDate)
					.build();

			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

			// Apply the HMAC protection
			signedJWT.sign(jwtProperties.getJWSSigner());

			// Serialize to compact form, produces something like
			// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
			String token = signedJWT.serialize();

			return AuthResultDto.builder()
					.token(token)
					.user(user.getUsername())
					.issuedAt(createdDate)
					.expiresAt(expirationDate)
					.build();
		} catch (JOSEException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getCause());
		}
	}
	
	public Mono<AuthResultDto> authenticate(Mono<UserLoginDto> userLoginMono) {
		return userLoginMono.flatMap(userLogin -> {
			return userRepository.findById(userLogin.getUsername())
					.filter(User::isEnabled)
					.filter(user -> passwordEncoder.matches(userLogin.getPassword(), user.getPassword()))
					.map(this::generateAccessToken)
					.switchIfEmpty(Mono.error(new FailedLoginException("Failed Login!")));
		});
	}
}
