package com.github.karlnicholas.webfluxjwtsecurity;

import java.security.Key;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.r2dbc.spi.ConnectionFactory;

/**
 * WebfluxSecurityApplication class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@SpringBootApplication
public class WebfluxSecurityApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebfluxSecurityApplication.class, args);
	}
    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));

        return initializer;
    }
    
    @Bean
    public Key getSecretKey() {
    	return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
