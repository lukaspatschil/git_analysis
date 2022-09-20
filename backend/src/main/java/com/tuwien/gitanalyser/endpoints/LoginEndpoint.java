package com.tuwien.gitanalyser.endpoints;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginEndpoint {

    @GetMapping("/oauth2/code/github")
    public String loginEndpoint(OAuth2AuthenticationToken token){
        return "Hello, " + token.toString();
    }
}
