package com.github.karlnicholas.webfluxjwtsecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.github.karlnicholas.webfluxjwtsecurity.dto.UserDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.mapper.UserMapper;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

@Component
public class UserHandler {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserHandler(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

	@PreAuthorize("hasRole('USER')")
	public Mono<ServerResponse> handleUser(ServerRequest serverRequest) {
		return ServerResponse.ok().body(ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new IllegalStateException("No SecurityContext")))
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userRepository::findById)
                .map(userMapper::mapToDto), UserDto.class);
	}

}
