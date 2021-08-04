package com.github.karlnicholas.webfluxjwtsecurity.dto.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.karlnicholas.webfluxjwtsecurity.dto.UserDto;
import com.github.karlnicholas.webfluxjwtsecurity.model.User;

/**
 * UserMapper class
 *
 * @author Karl Nicholas
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
	UserDto mapToDto(User user);

	@InheritInverseConfiguration
    @Mapping(target = "username")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
	@Mapping(target = "isNew", ignore = true)
    User mapToUser(UserDto userDto);
}
