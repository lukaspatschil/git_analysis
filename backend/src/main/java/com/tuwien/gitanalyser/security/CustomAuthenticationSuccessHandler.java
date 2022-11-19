package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public CustomAuthenticationSuccessHandler(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {

        BasicAuth2User oauthUser;

        if (((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().equals("github")) {
            oauthUser = new GitHubOAuth2User((OAuth2User) authentication.getPrincipal());
        } else {
            oauthUser = new GitLabOAuth2User((OAuth2User) authentication.getPrincipal());
        }

        userService.processOAuthPostLogin(oauthUser);

        response.sendRedirect("/apiV1/repository");
    }
}
