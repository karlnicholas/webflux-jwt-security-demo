package com.github.karlnicholas.webfluxjwtsecurity.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.github.karlnicholas.webfluxjwtsecurity.dto.AuthResultDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.UserLoginDto;
import com.github.karlnicholas.webfluxjwtsecurity.service.AuthService;

import reactor.core.publisher.Mono;

@Component
public class AuthHandler {
    private final AuthService loginService;

    public AuthHandler(AuthService loginService) {
        this.loginService = loginService;
    }

	public Mono<ServerResponse> handleLogin(ServerRequest serverRequest) {
		return ServerResponse.ok().body(loginService.authenticate(serverRequest.bodyToMono(UserLoginDto.class)), AuthResultDto.class);
	}
}
