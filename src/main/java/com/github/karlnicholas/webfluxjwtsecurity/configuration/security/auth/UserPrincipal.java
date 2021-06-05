package com.github.karlnicholas.webfluxjwtsecurity.configuration.security.auth;

import java.security.Principal;

/**
 * UserPrincipal class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
public class UserPrincipal implements Principal {
    private Long id;
    private String name;

    public UserPrincipal(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
