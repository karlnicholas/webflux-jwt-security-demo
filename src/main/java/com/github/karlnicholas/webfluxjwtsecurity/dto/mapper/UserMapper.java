package com.github.karlnicholas.webfluxjwtsecurity.dto.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    UserDto mapToUser(User user);

    @InheritInverseConfiguration
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User mapToDto(UserDto userDto);
}
