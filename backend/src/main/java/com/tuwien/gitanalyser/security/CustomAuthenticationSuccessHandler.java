package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.security.oauth2.BasicAuth2User;
import com.tuwien.gitanalyser.security.oauth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.oauth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    private final JWTTokenProvider jwtTokenProviderImpl;

    public CustomAuthenticationSuccessHandler(final UserService userService,
                                              @Lazy final OAuth2AuthorizedClientRepository authorizedClientRepository,
                                              final JWTTokenProvider jwtTokenProviderImpl) {
        this.userService = userService;
        this.authorizedClientRepository = authorizedClientRepository;
        this.jwtTokenProviderImpl = jwtTokenProviderImpl;
    }

    private static BasicAuth2User getAuth2User(final Authentication authentication) {
        BasicAuth2User oauthUser;
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;

        if (authenticationToken.getAuthorizedClientRegistrationId().equals("github")) {
            oauthUser = new GitHubOAuth2User((OAuth2User) authentication.getPrincipal());
        } else {
            oauthUser = new GitLabOAuth2User((OAuth2User) authentication.getPrincipal());
        }
        return oauthUser;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {

        BasicAuth2User oauthUser = getAuth2User(authentication);

        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient authorizedClient =
            authorizedClientRepository.loadAuthorizedClient(authenticationToken.getAuthorizedClientRegistrationId(),
                                                            authentication,
                                                            request);

        // TODO get refresh token

        User user = userService.processOAuthPostLogin(oauthUser, authorizedClient.getAccessToken().getTokenValue());

        response.sendRedirect(AuthenticationConstants.FRONTEND_REDIRECT_AFTER_LOGIN_URL
                                  + "#"
                                  + jwtTokenProviderImpl.createToken(user.getId()));
    }
}
