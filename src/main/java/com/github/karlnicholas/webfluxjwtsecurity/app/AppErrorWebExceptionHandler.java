package com.github.karlnicholas.webfluxjwtsecurity.app;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;

/**
 * AppErrorWebExceptionHandler class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Component
@Order(-2)
public class AppErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    public AppErrorWebExceptionHandler(AppErrorAttributes g, ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(g, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), request -> {
            var props = getErrorAttributes(request, ErrorAttributeOptions.defaults());

            return ServerResponse.status(Integer.parseInt(props.getOrDefault("status", 500).toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(props.get("errors")));
        });
    }
}
