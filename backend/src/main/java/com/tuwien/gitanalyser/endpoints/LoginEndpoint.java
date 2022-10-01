package com.tuwien.gitanalyser.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginEndpoint.class);
    
    @GetMapping("/login/success")
    public String loginSuccessful(OAuth2AuthenticationToken authentication) {
        LOGGER.info("LoginEndpoint: Login successful; " + authentication.toString());
        return "Login successful; " + authentication;
    }

    @GetMapping("/login/failure")
    public String loginFailure() {
        LOGGER.info("LoginEndpoint: Login failed");
        return "Login failed";
    }

    @GetMapping("/users")
    public OAuth2User user(@AuthenticationPrincipal OAuth2User principal) {
        return principal;
    }
}
