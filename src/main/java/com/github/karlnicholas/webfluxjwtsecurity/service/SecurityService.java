package com.github.karlnicholas.webfluxjwtsecurity.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.karlnicholas.webfluxjwtsecurity.configuration.security.auth.TokenInfo;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

import java.io.Serializable;
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
@Component
public class SecurityService implements Serializable {
	private static final long serialVersionUID = 1L;
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
        return doGenerateToken(claims, user.getUsername(), user.getId().toString());
    }

    private TokenInfo doGenerateToken(Map<String, Object> claims, String issuer, String subject) {
        var expirationTimeInMilliseconds = Long.parseLong(defaultExpirationTimeInSecondsConf) * 1000;
        var expirationDate = new Date(new Date().getTime() + expirationTimeInMilliseconds);

        return doGenerateToken(expirationDate, claims, issuer, subject);
    }

    private TokenInfo doGenerateToken(Date expirationDate, Map<String, Object> claims, String issuer, String subject) {
        var createdDate = new Date();
        var token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
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
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (!user.isEnabled())
                        return Mono.error(new AccountLockedException("Account disabled."));

                    if (!passwordEncoder.encode(password).equals(user.getPassword()))
                        return Mono.error(new FailedLoginException("Failed Login!"));

                    return Mono.just(generateAccessToken(user).toBuilder()
                            .userId(user.getId())
                            .build());
                })
                .switchIfEmpty(Mono.error(new FailedLoginException("Failed Login!")));
    }
}
