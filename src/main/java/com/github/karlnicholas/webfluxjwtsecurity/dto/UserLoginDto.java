package com.github.karlnicholas.webfluxjwtsecurity.dto;

import lombok.Data;

/**
 * UserLoginDto class
 *
 * @author Karl Nicholas
 */
@Data
public class UserLoginDto {
    private String username;
    private String password;
}
