package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.DTOs.UserDTO;
import com.tuwien.gitanalyser.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO entityToDTO(User user);

    User dtoToEntity(UserDTO userDTO);

    List<UserDTO> entitiesToDTOs(List<User> allPeople);
}
