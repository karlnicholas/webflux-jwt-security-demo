package com.github.karlnicholas.webfluxjwtsecurity.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.dto.AuthResultDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.UserLoginDto;
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
public class AuthService {
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Key secretKey;

    @Value("${jwt.expiration}")
    private String defaultExpirationTimeInSecondsConf;

    public AuthService(Key secretKey, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    	this.secretKey = secretKey;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private AuthResultDto generateAccessToken(User user) {
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

        return AuthResultDto.builder()
                .token(token)
                .username(user.getUsername())
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

	public Mono<AuthResultDto> authenticate(Mono<UserLoginDto> userLoginMono) {
		return userLoginMono.flatMap(userLogin->{
			return userRepository.findByUsername(userLogin.getUsername())
			.flatMap(user->{
				if (!user.isEnabled())
					return Mono.error(new AccountLockedException("Account disabled."));
				if (!passwordEncoder.matches(userLogin.getPassword(), user.getPassword()))
					return Mono.error(new FailedLoginException("Failed Login!"));
				return Mono.just(generateAccessToken(user));
			});
		})
		.switchIfEmpty(Mono.error(new FailedLoginException("Failed Login!")));
	}
}
