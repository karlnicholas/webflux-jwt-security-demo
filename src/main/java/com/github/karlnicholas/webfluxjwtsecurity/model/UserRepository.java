package com.github.karlnicholas.webfluxjwtsecurity.model;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

/**
 * UserRepository class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, String> {
	Mono<User> findByUsername(String username);
}
