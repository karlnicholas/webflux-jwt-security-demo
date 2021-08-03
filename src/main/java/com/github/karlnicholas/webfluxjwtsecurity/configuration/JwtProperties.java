package com.github.karlnicholas.webfluxjwtsecurity.configuration;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

@Component
@ConfigurationProperties(prefix = "jwt")
@Configuration
public class JwtProperties {
	private Long expiration;
	private String sharedSecretHex;
	// Create HMAC signer
	private JWSSigner jWSSigner;
	private JWSVerifier jWSVerifier;
	private byte[] secret;

	private byte[] getSecret() {
		if ( secret == null ) {
			secret = new byte[32];
			int l = sharedSecretHex.length()/2;
			for (int i = 0; i < l; i++) {
			   int j = Integer.parseInt(sharedSecretHex.substring(i*2, i*2+2), 16);
			   secret[i] = (byte) j;
			}
		}
		return secret;
	}

    public JWSSigner getJWSSigner() {
    	if ( jWSSigner == null ) {
			try {
				jWSSigner = new MACSigner(getSecret());
			} catch (KeyLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return jWSSigner;
    }
    public JWSVerifier getJWSVerifier() {
    	if ( jWSVerifier == null ) {
    		try {
				jWSVerifier = new MACVerifier(getSecret());
			} catch (JOSEException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return jWSVerifier;
    }

	public Optional<SignedJWT> verifyToken(String token) {
		if ( token == null || token.isEmpty()) {
			return Optional.empty();
		}
		// On the consumer side, parse the JWS and verify its HMAC
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			boolean valid = true;
			valid &= signedJWT.verify(getJWSVerifier());
			valid &= new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());
			if ( !valid) {
				return Optional.empty();
			}
	        return Optional.of(signedJWT);
		} catch (ParseException | JOSEException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public Long getExpiration() {
		return expiration;
	}

	public void setExpiration(Long expiration) {
		this.expiration = expiration;
	}

	public String getSharedSecretHex() {
		return sharedSecretHex;
	}

	public void setSharedSecretHex(String sharedSecretHex) {
		this.sharedSecretHex = sharedSecretHex;
	}

}
