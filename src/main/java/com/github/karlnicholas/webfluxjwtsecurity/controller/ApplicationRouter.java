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

@Configuration
public class ApplicationRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(PublicHandler publicHandler, AuthHandler authHandler, UserHandler userHandler) {
        return RouterFunctions.route(POST("/login").and(accept(MediaType.APPLICATION_JSON)), authHandler::handleLogin)
    		.andRoute(POST("/public/demo-user").and(accept(MediaType.APPLICATION_JSON)), publicHandler::handleDemoUser)
			.andRoute(GET("/public/version").and(accept(MediaType.APPLICATION_JSON)), publicHandler::handleVersion)
			.andRoute(GET("/user").and(accept(MediaType.APPLICATION_JSON)), userHandler::handleUser)
		;
    }
}