package com.github.karlnicholas.webfluxjwtsecurity.controller;

import java.text.ParseException;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.github.karlnicholas.webfluxjwtsecurity.configuration.JwtProperties;
import com.github.karlnicholas.webfluxjwtsecurity.dto.AuthResultDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.UserLoginDto;
import com.github.karlnicholas.webfluxjwtsecurity.service.AuthService;

import reactor.core.publisher.Mono;

@Component
public class AuthHandler {
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthHandler(
		AuthService authService, 
		JwtProperties jwtProperties
	) {
    	this.jwtProperties = jwtProperties;
        this.authService = authService;
    }

	public Mono<ServerResponse> handleLogin(ServerRequest serverRequest) {
		return ServerResponse.ok().body(authService.authenticate(serverRequest.bodyToMono(UserLoginDto.class)), AuthResultDto.class);
	}

	public Mono<ServerResponse> verifyToken(ServerRequest serverRequest) {
		return ServerResponse.ok().bodyValue(serverRequest.queryParam("token").map(token->
			jwtProperties.verifyToken(token).map(signedJwt->{
				try {
					return AuthResultDto.builder()
						.token(signedJwt.serialize())
						.user(signedJwt.getJWTClaimsSet().getSubject())
						.issuedAt(signedJwt.getJWTClaimsSet().getIssueTime())
						.expiresAt(signedJwt.getJWTClaimsSet().getExpirationTime())
						.build();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}).orElseThrow()
		).orElseThrow());
	}
}
