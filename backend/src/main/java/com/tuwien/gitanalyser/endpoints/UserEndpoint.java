package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.UserDTO;
import com.tuwien.gitanalyser.entity.mapper.UserMapper;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);
    private final UserService userService;

    private final UserMapper userMapper;

    public UserEndpoint(final UserService userService, final UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * get user by id.
     * @param id of the user
     * @return single user DTO
     */
    @GetMapping(path = "/{id}")
    public UserDTO getUser(final @PathVariable Long id) {
        LOGGER.info("UserEndpoint: Get user with id " + id);
        return userMapper.entityToDTO(userService.getUser(id));
    }

    /**
     * get all users.
     * @return a list of users
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        LOGGER.info("GET /user -  get all users");
        return userMapper.entitiesToDTOs(userService.getAll());
    }

}
