package com.github.karlnicholas.webfluxjwtsecurity;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	private byte[] sharedSecret = null; 

	@Value("${jwt.shared_secret_hex}")
	private String sharedSecretHex;

    @Bean
    public byte[] getSecret() {
    	if ( sharedSecret == null ) {
    		sharedSecret = new byte[32];
    		int l = sharedSecretHex.length()/2;
    		for (int i = 0; i < l; i++) {
			   int j = Integer.parseInt(sharedSecretHex.substring(i*2, i*2+2), 16);
			   sharedSecret[i] = (byte) j;
			}
		}
    	return sharedSecret;
    }
}
