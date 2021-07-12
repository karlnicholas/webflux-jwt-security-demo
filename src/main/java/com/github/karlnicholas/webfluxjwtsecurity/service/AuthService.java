package com.github.karlnicholas.webfluxjwtsecurity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.dto.AuthResultDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.UserLoginDto;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

import java.util.*;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.FailedLoginException;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;

/**
 * SecurityService class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final byte[] sharedSecret;

	@Value("${jwt.expiration}")
	private String defaultExpirationTimeInSecondsConf;

	public AuthService(byte[] sharedSecret, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.sharedSecret = sharedSecret;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	private AuthResultDto generateAccessToken(User user) {
		var claims = new HashMap<String, Object>();
		claims.put("role", user.getRoles());
		var expirationTimeInMilliseconds = Long.parseLong(defaultExpirationTimeInSecondsConf) * 1000;
		var expirationDate = new Date(new Date().getTime() + expirationTimeInMilliseconds);
		var createdDate = new Date();

		try {

			// Create HMAC signer
			JWSSigner signer = new MACSigner(sharedSecret);

			// Prepare JWT with claims set
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
					.claim("role", user.getRoles())
					.subject(user.getUsername())
					.issueTime(createdDate)
					.expirationTime(expirationDate)
					.build();

			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

			// Apply the HMAC protection
			signedJWT.sign(signer);

			// Serialize to compact form, produces something like
			// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
			String token = signedJWT.serialize();

			return AuthResultDto.builder()
					.token(token)
					.username(user.getUsername())
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
			return userRepository.findByUsername(userLogin.getUsername()).flatMap(user -> {
				if (!user.isEnabled())
					return Mono.error(new AccountLockedException("Account disabled."));
				if (!passwordEncoder.matches(userLogin.getPassword(), user.getPassword()))
					return Mono.error(new FailedLoginException("Failed Login!"));
				return Mono.just(generateAccessToken(user));
			});
		}).switchIfEmpty(Mono.error(new FailedLoginException("Failed Login!")));
	}
}
