package com.github.karlnicholas.webfluxjwtsecurity.model;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository class
 *
 * @author Karl Nicholas
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, String> {
}
