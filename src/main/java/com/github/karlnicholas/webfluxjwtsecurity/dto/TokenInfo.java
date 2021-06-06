package com.github.karlnicholas.webfluxjwtsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * TokenInfo class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private String userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}
