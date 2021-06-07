package com.github.karlnicholas.webfluxjwtsecurity.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.webfluxjwtsecurity.model.User;
import com.github.karlnicholas.webfluxjwtsecurity.model.UserRepository;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * UserService class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(Collections.singletonList("ROLE_USER"))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build())
                .doOnSuccess(u -> log.info("Created new user with username = " + u.getUsername()));
    }

    public Mono<User> getUser(String username) {
        return userRepository.findByUsername(username);
    }
}
