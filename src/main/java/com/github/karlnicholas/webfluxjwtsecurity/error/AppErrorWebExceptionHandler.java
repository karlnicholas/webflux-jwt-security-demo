package com.github.karlnicholas.webfluxjwtsecurity.error;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.FailedLoginException;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;

import com.nimbusds.jose.JOSEException;

/**
 * AppErrorWebExceptionHandler class
 *
 * @author Karl Nicholas
 */
@Component
public class AppErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
	public AppErrorWebExceptionHandler(ErrorAttributes g, ApplicationContext applicationContext,
			ServerCodecConfigurer serverCodecConfigurer) {
		super(g, new WebProperties.Resources(), applicationContext);
		super.setMessageWriters(serverCodecConfigurer.getWriters());
		super.setMessageReaders(serverCodecConfigurer.getReaders());
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
		return RouterFunctions.route(POST("/api/auth/signin"), request -> {
			var error = getError(request);
			if (error instanceof SecurityException || error instanceof JOSEException
					|| error instanceof AccountLockedException || error instanceof FailedLoginException) {
				return ServerResponse.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(error.getMessage()));
			} else {
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(error.getMessage()));
			}
		});
	}
}
