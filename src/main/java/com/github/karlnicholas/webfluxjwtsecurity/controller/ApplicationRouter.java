package com.github.karlnicholas.webfluxjwtsecurity.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Configuration
public class ApplicationRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(PublicHandler publicHandler, AuthHandler authHandler, UserHandler userHandler) {
        return RouterFunctions.nest(accept(MediaType.APPLICATION_JSON), 
        		RouterFunctions.nest(POST("/api/auth").and(contentType(MediaType.APPLICATION_JSON)), 
    				RouterFunctions.route(path("/signup"), publicHandler::handleNewUser)
    				.andRoute(path("/signin"), authHandler::handleLogin))
			.andRoute(GET("/verifyToken"), authHandler::verifyToken)
			.andRoute(GET("/public/version"), publicHandler::handleVersion)
			.andRoute(GET("/profile"), userHandler::handleUser));
    }
}