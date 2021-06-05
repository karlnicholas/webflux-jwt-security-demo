package com.github.karlnicholas.webfluxjwtsecurity.service;

import javax.security.auth.login.AccountLockedException;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.karlnicholas.webfluxjwtsecurity.configuration.security.auth.UserPrincipal;

import reactor.core.publisher.Mono;

/**
 * AuthenticationManager class
 * It is used in AuthenticationFilter.
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final UserService userService;

    public AuthenticationManager(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var principal = (UserPrincipal) authentication.getPrincipal();

        //TODO add more user validation logic here.
        return userService.getUser(principal.getId())
                .filter(user -> user.isEnabled())
                .switchIfEmpty(Mono.error(new AccountLockedException ("User account is disabled.")))
                .map(user -> authentication);
    }
}
