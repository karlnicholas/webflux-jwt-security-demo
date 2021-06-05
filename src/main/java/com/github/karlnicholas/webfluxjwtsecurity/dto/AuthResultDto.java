package com.github.karlnicholas.webfluxjwtsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * AuthResultDto class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AuthResultDto {
    private Long userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}
