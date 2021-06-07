package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
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
    private final Function<ServerWebExchange, Optional<String>> extractTokenFunction;
    private final JwtParser jwtParser;
    public AppServerAuthenticationConverter(JwtParser jwtParser, Function<ServerWebExchange, Optional<String>> extractTokenFunction) {
    	this.jwtParser = jwtParser;
    	this.extractTokenFunction = extractTokenFunction;
    }
	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
    	return Mono.justOrEmpty(create(exchange));
	}
    
	private Optional<Authentication> create(ServerWebExchange serverWebExchange) {
		try {
			return extractTokenFunction.apply(serverWebExchange).map(token->{
		    	Claims claims = (Claims) jwtParser.parse(token).getBody();
		        var subject = claims.getSubject();
		        @SuppressWarnings("unchecked")
				List<String> roles = claims.get("role", List.class);
		        var authorities = roles.stream()
		                .map(SimpleGrantedAuthority::new)
		                .collect(Collectors.toList());
		        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
			});
		} catch ( Throwable t) {
			if ( t.getMessage() != null )
				log.warn(t.getMessage());
			return Optional.empty();
		}
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

	public static Optional<String> getCookieToken(ServerWebExchange serverWebExchange) {
		HttpCookie cookie = serverWebExchange
			.getRequest()
			.getCookies()
			.getFirst("X-Session-Id");
		if ( cookie == null )
        	return Optional.empty();
        return Optional.of(cookie.getValue());
	}
}
