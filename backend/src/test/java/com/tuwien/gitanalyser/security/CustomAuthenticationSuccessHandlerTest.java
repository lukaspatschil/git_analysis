package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.security.OAuth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.apache.catalina.connector.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CustomAuthenticationSuccessHandlerTest {

    private CustomAuthenticationSuccessHandler sut;
    private Request request;
    private HttpServletResponse response;
    private OAuth2AuthenticationToken gitHubAuthentication;
    private OAuth2AuthenticationToken gitLabAuthentication;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        sut = new CustomAuthenticationSuccessHandler(userService);

        request = mock(Request.class);
        response = mock(HttpServletResponse.class);

        var oAuth2User = mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        var gitHubOAuth2User = new GitHubOAuth2User(oAuth2User);
        var gitLabOAuth2User = new GitLabOAuth2User(oAuth2User);

        gitLabAuthentication = new OAuth2AuthenticationToken(gitLabOAuth2User, null,
                                                             GitLabOAuthProviderProperties.REGISTRATION_ID);
        gitHubAuthentication = new OAuth2AuthenticationToken(gitHubOAuth2User, null,
                                                             GitHubOAuthProviderProperties.REGISTRATION_ID);
    }

    @Test
    public void onAuthenticationSuccess_gitLabAuthentication_redirectsToStartPage() throws IOException {
        // Given

        // When
        sut.onAuthenticationSuccess(request, response, gitLabAuthentication);

        // Then
        verify(response).sendRedirect("/apiV1/repository");
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_redirectsToStartPage() throws IOException {
        // Given

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(response).sendRedirect("/apiV1/repository");
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_shouldCallProcessOAuthPostLoginOfUserService() throws IOException {
        // Given

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any());
    }

    @Test
    public void onAuthenticationSuccess_gitLabAuthentication_shouldCallProcessOAuthPostLoginOfUserService() throws IOException {
        // Given

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any());
    }

}