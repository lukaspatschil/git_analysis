package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.UserDTO;
import com.tuwien.gitanalyser.entity.mapper.UserMapper;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/apiV1/user")
@SecurityRequirement(name = "oauth2")
@Tag(name = "User Endpoint")
public class UserEndpoint extends BaseEndpoint {

    private final UserService userService;

    private final UserMapper userMapper;

    public UserEndpoint(final UserService userService, final UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping()
    @Operation(description = "Get data of the logged in user", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            schema = @Schema(implementation = UserDTO.class),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public UserDTO getLoggedInUser(final Authentication authentication) throws NotFoundException {
        long userId = getUserId(authentication);
        return userMapper.entityToDTO(userService.getUser(userId));
    }
}
