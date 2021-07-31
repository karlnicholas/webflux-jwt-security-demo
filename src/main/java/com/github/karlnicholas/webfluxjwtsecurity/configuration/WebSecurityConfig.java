package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

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

    @Value("${app.public_routes}")
    private String[] publicRoutes;
    private final JwtProperties jwtProperties;
    
    public WebSecurityConfig(JwtProperties jwtProperties) {
    	this.jwtProperties = jwtProperties;
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
                .logout(logout -> logout.requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout")))
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
                .addFilterAt(createAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    AuthenticationWebFilter createAuthenticationFilter(ReactiveAuthenticationManager authManager) {
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(authManager);
        authenticationFilter.setServerAuthenticationConverter(exchange->{
        	return Mono.justOrEmpty(getBearerToken(exchange).map(token->{
			// On the consumer side, parse the JWS and verify its HMAC
			try {
				SignedJWT signedJWT = SignedJWT.parse(token);
				boolean valid = true;
				valid &= signedJWT.verify(jwtProperties.getJWSVerifier());
				valid &= new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());
				if ( !valid) {
					return null;
				}
				com.nimbusds.jose.shaded.json.JSONArray jsonArray = (com.nimbusds.jose.shaded.json.JSONArray) signedJWT.getJWTClaimsSet().getClaim("role");
				SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority((String) jsonArray.get(0));

		        return new UsernamePasswordAuthenticationToken(signedJWT.getJWTClaimsSet().getSubject(), null, Collections.singletonList(simpleGrantedAuthority));
			} catch (ParseException | JOSEException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}));});
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return authenticationFilter;
    }

	@Bean
    public ReactiveAuthenticationManager authenticationManager() {
    	return Mono::just;
    }

	private static final String BEARER = "Bearer ";

	public static Optional<String> getBearerToken(ServerWebExchange serverWebExchange) {
        String token = serverWebExchange.getRequest()
        		.getHeaders()
        		.getFirst(HttpHeaders.AUTHORIZATION);
        if ( token == null )
        	return Optional.empty();
        if ( token.length() <= BEARER.length() )
        	return Optional.empty();
        return Optional.of(token.substring(BEARER.length()));
	}


}
