package com.github.karlnicholas.webfluxjwtsecurity.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ServerHttpBearerAuthenticationConverter class
 * This is a Converter that validates TOKEN against requests coming from AuthenticationFilter ServerWebExchange.
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
public class AppServerAuthenticationConverter implements ServerAuthenticationConverter {
	Logger log = LoggerFactory.getLogger(AppServerAuthenticationConverter.class);
    private final Function<ServerWebExchange, String> extractTokenFunction;
    private final JwtParser jwtParser;
    public AppServerAuthenticationConverter(JwtParser jwtParser, Function<ServerWebExchange, String> extractTokenFunction) {
    	this.jwtParser = jwtParser;
    	this.extractTokenFunction = extractTokenFunction;
    }
	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
    	return Mono.just(create(exchange))
    			.filter(Optional::isPresent)
				.map(Optional::get);
	}
    
	private Optional<Authentication> create(ServerWebExchange serverWebExchange) {
		try {
	    	Claims claims = (Claims) jwtParser.parse(extractTokenFunction.apply(serverWebExchange)).getBody();
	        var subject = claims.getSubject();
	        @SuppressWarnings("unchecked")
			List<String> roles = claims.get("role", List.class);
	        var authorities = roles.stream()
	                .map(SimpleGrantedAuthority::new)
	                .collect(Collectors.toList());
	        return Optional.of(new UsernamePasswordAuthenticationToken(subject, null, authorities));
		} catch ( Throwable t) {
			if ( t.getMessage() != null )
				log.warn(t.getMessage());
			return Optional.empty();
		}
    }

}
