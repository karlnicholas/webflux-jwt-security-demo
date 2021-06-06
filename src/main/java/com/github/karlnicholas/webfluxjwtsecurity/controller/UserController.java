package com.github.karlnicholas.webfluxjwtsecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.karlnicholas.webfluxjwtsecurity.dto.UserDto;
import com.github.karlnicholas.webfluxjwtsecurity.dto.mapper.UserMapper;
import com.github.karlnicholas.webfluxjwtsecurity.service.UserService;

import reactor.core.publisher.Mono;

/**
 * UserController class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@RestController
@RequestMapping("user")
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Mono<UserDto> get(Authentication authentication) {
        return userService.getUser((String)authentication.getPrincipal())
                .map(user -> userMapper.map(user));
    }
}
