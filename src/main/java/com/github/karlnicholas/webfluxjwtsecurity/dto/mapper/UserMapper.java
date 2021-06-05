package com.github.karlnicholas.webfluxjwtsecurity.dto.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.github.karlnicholas.webfluxjwtsecurity.dto.UserDto;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;

/**
 * UserMapper class
 *
 * @author Erik Amaru Ortiz
 * @author Karl Nicholas
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto map(User user);

    @InheritInverseConfiguration
    User map(UserDto userDto);
}
