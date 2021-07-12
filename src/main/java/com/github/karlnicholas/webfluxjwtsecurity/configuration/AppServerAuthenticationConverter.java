package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

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
    private final JWSVerifier verifier;
    public AppServerAuthenticationConverter(byte[] sharedSecret, Function<ServerWebExchange, Optional<String>> extractTokenFunction) throws JOSEException {
		verifier = new MACVerifier(sharedSecret);
    	this.extractTokenFunction = extractTokenFunction;
    }
	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
    	return Mono.justOrEmpty(create(exchange));
	}
    
	private Optional<Authentication> create(ServerWebExchange serverWebExchange) {
		return extractTokenFunction.apply(serverWebExchange).map(token->{
			// On the consumer side, parse the JWS and verify its HMAC
			try {
				SignedJWT signedJWT = SignedJWT.parse(token);
				boolean valid = true;
				valid &= signedJWT.verify(verifier);
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
		});
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
