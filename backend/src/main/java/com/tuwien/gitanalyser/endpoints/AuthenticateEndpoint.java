package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.RefreshAccessTokenDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RefreshAuthenticationDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController()
@RequestMapping("/apiV1/refresh")
@Tag(name = "Refresh Endpoint")
public class AuthenticateEndpoint extends BaseEndpoint {

    private final UserService userService;

    public AuthenticateEndpoint(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @Operation(description = "Get new access token", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            schema = @Schema(implementation = RefreshAuthenticationDTO.class),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public RefreshAuthenticationDTO refreshToken(
        final @RequestBody RefreshAccessTokenDTO refreshAccessTokenDTO,
        final @CookieValue("fingerprint") String fingerprint,
        final HttpServletResponse response)
        throws NotFoundException {

        RefreshAuthenticationInternalDTO refreshAuthenticationInternalDTO = userService.refreshAccessToken(
            refreshAccessTokenDTO.getRefreshToken(),
            fingerprint);

        response.addCookie(new Cookie("fingerprint", refreshAuthenticationInternalDTO.getFingerprint()));
        return new RefreshAuthenticationDTO(
            refreshAuthenticationInternalDTO.getAccessToken(),
            refreshAuthenticationInternalDTO.getRefreshToken());

    }
}
