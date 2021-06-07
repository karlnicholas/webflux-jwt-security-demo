package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Optional;
import java.util.function.Function;

import javax.security.auth.login.AccountLockedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

import com.github.karlnicholas.webfluxjwtsecurity.service.UserService;

import reactor.core.publisher.Mono;

/**
 * WebSecurityConfig class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {
    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    private final UserService userService;

    private final JwtParser jwtParser;
    @Value("${app.public_routes}")
    private String[] publicRoutes;
    
    public WebSecurityConfig(Key secretKey, UserService userService) {
    	this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
        this.userService = userService;
    }
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authManager) {
        return http
                .authorizeExchange()
                    .pathMatchers(HttpMethod.OPTIONS)
                        .permitAll()
                    .pathMatchers(publicRoutes)
                        .permitAll()
                    .pathMatchers( "/favicon.ico")
                        .permitAll()
                    .anyExchange()
                        .authenticated()
                    .and()
                .csrf()
                    .disable()
                .httpBasic()
                    .disable()
                .formLogin()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint((swe, e) -> {
                        logger.info("[1] Authentication error: Unauthorized[401]: " + e.getMessage());

                        return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                    })
                    .accessDeniedHandler((swe, e) -> {
                        logger.info("[2] Authentication error: Access Denied[401]: " + e.getMessage());

                        return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                    })
                .and()
                .addFilterAt(createAuthenticationFilter(authManager, AppServerAuthenticationConverter::getBearerToken), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(createAuthenticationFilter(authManager, AppServerAuthenticationConverter::getCookieToken), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    AuthenticationWebFilter createAuthenticationFilter(ReactiveAuthenticationManager authManager, Function<ServerWebExchange, Optional<String>> extractTokenFunction) {
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(authManager);
        authenticationFilter.setServerAuthenticationConverter( new AppServerAuthenticationConverter(jwtParser, extractTokenFunction));
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return authenticationFilter;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
    	return new ReactiveAuthenticationManager() {
			@Override
			public Mono<Authentication> authenticate(Authentication authentication) {
				return userService.getUser((String)authentication.getPrincipal())
                    .filter(user -> user.isEnabled())
                    .switchIfEmpty(Mono.error(new AccountLockedException ("User account is disabled.")))
                    .map(user -> authentication);
			}
    	};
    }

}
