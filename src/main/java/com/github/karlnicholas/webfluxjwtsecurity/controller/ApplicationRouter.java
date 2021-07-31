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
			.andRoute(GET("/public/version"), publicHandler::handleVersion)
			.andRoute(GET("/user"), userHandler::handleUser));
        		
//		RouterFunctions.route(POST("/signup"), publicHandler.handleDemoUser(sr))
//		.andRoute(POST("/signin")), authHandler.handleLogin(sr));

//		return RouterFunctions.route(POST("/signin").and(accept(MediaType.APPLICATION_JSON)), authHandler::handleLogin)
//    		.andRoute(POST("/api/auth/signup").and(accept(MediaType.APPLICATION_JSON)), publicHandler::handleDemoUser)
//			.andRoute(GET("/public/version").and(accept(MediaType.APPLICATION_JSON)), publicHandler::handleVersion)
//			.andRoute(GET("/user").and(accept(MediaType.APPLICATION_JSON)), userHandler::handleUser)
//		;
    }
}