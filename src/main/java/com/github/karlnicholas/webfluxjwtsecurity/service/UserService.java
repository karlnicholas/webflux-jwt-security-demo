package com.github.karlnicholas.webfluxjwtsecurity.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.model.User;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * UserService class
 *
 * @author Karl Nicholas
 */
@Slf4j
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final R2dbcEntityOperations r2dbcEntityOperations;

    public UserService(R2dbcEntityOperations r2dbcEntityOperations, PasswordEncoder passwordEncoder) {
        this.r2dbcEntityOperations = r2dbcEntityOperations;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> createUser(User user) {
        return r2dbcEntityOperations.insert(user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(Collections.singletonList("ROLE_USER"))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build())
                .doOnSuccess(u -> log.info("Created new user with username = " + u.getUsername()));
    }

}
