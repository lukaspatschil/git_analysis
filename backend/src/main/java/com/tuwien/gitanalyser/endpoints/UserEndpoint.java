package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.UserDTO;
import com.tuwien.gitanalyser.entity.mapper.UserMapper;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/apiV1/user")
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);

    private final UserService userService;

    private final UserMapper userMapper;

    public UserEndpoint(final UserService userService, final UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping()
    public UserDTO getLoggedInUser(final Authentication authentication) throws NotFoundException {
        LOGGER.info("GET /user -  get data of the logged in user");
        return userMapper.entityToDTO(
            userService.getUser(Long.parseLong(authentication.getName()))
        );
    }
}
