package com.github.karlnicholas.webfluxjwtsecurity.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.dto.TokenInfo;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.FailedLoginException;

/**
 * SecurityService class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Service
public class SecurityService {
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Key secretKey;

    @Value("${jwt.expiration}")
    private String defaultExpirationTimeInSecondsConf;

    public SecurityService(Key secretKey, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    	this.secretKey = secretKey;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private TokenInfo generateAccessToken(User user) {
        var claims = new HashMap<String, Object>();
        claims.put("role", user.getRoles());
        var expirationTimeInMilliseconds = Long.parseLong(defaultExpirationTimeInSecondsConf) * 1000;
        var expirationDate = new Date(new Date().getTime() + expirationTimeInMilliseconds);
        var createdDate = new Date();
        var token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact();

        return TokenInfo.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

	public Mono<TokenInfo> authenticate(String username, String password) {
		return userRepository.findByUsername(username).flatMap(user -> {
			if (!user.isEnabled())
				return Mono.error(new AccountLockedException("Account disabled."));
			if (!passwordEncoder.encode(password).equals(user.getPassword()))
				return Mono.error(new FailedLoginException("Failed Login!"));
			return Mono.just(generateAccessToken(user).toBuilder()
				.userId(user.getId().toString())
				.build());
		})
		.switchIfEmpty(Mono.error(new FailedLoginException("Failed Login!")));
	}
}
