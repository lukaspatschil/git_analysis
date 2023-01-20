package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.security.OAuth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.service.UserService;
import org.apache.catalina.connector.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import utils.Randoms;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAuthenticationSuccessHandlerTest {

    private static final Date DATE = new Date();
    private CustomAuthenticationSuccessHandler sut;
    private Request request;
    private HttpServletResponse response;
    private OAuth2AuthenticationToken gitHubAuthentication;
    private OAuth2AuthenticationToken gitLabAuthentication;

    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    private UserService userService;
    private JWTTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        jwtTokenProvider = mock(JWTTokenProvider.class);

        sut = new CustomAuthenticationSuccessHandler(userService, authorizedClientRepository, jwtTokenProvider);

        request = mock(Request.class);
        response = mock(HttpServletResponse.class);

        var oAuth2User = mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        var gitHubOAuth2User = new GitHubOAuth2User(oAuth2User);
        var gitLabOAuth2User = new GitLabOAuth2User(oAuth2User);

        gitLabAuthentication = new OAuth2AuthenticationToken(gitLabOAuth2User,
                                                             null,
                                                             AuthenticationConstants.GITLAB_REGISTRATION_ID);
        gitHubAuthentication = new OAuth2AuthenticationToken(gitHubOAuth2User,
                                                             null,
                                                             AuthenticationConstants.GITHUB_REGISTRATION_ID);
    }

    @Test
    public void onAuthenticationSuccess_gitLabAuthentication_redirectsToStartPage() throws IOException {
        // Given
        prepareAuthorizedClientRepository(gitLabAuthentication);
        String bearerToken = setUpJwtTokenProvider();

        // When
        sut.onAuthenticationSuccess(request, response, gitLabAuthentication);

        // Then
        verify(response).sendRedirect(AuthenticationConstants.FRONTEND_REDIRECT_AFTER_LOGIN_URL + "#" + bearerToken);
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_redirectsToStartPage() throws IOException {
        // Given
        prepareAuthorizedClientRepository(gitHubAuthentication);
        String bearerToken = setUpJwtTokenProvider();

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(response).sendRedirect(AuthenticationConstants.FRONTEND_REDIRECT_AFTER_LOGIN_URL + "#" + bearerToken);
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_shouldCallProcessOAuthPostLogin() throws IOException {
        // Given

        String tokenValue = prepareAuthorizedClientRepository(gitHubAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any(GitHubOAuth2User.class),
                                                  eq(tokenValue));
    }

    @Test
    public void onAuthenticationSuccess_gitLabAuthentication_shouldCallProcessOAuthPostLogin() throws IOException {
        // Given
        String tokenValue = prepareAuthorizedClientRepository(gitLabAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitLabAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any(GitLabOAuth2User.class),
                                                  eq(tokenValue));
    }

    private String prepareAuthorizedClientRepository(OAuth2AuthenticationToken authenticationProvider) {
        OAuth2AuthorizedClient auth2User = mock(OAuth2AuthorizedClient.class);

        when(authorizedClientRepository.loadAuthorizedClient(authenticationProvider.getAuthorizedClientRegistrationId(),
                                                             authenticationProvider,
                                                             request))
            .thenReturn(auth2User);

        String accessToken = mockAccessToken(auth2User);

        User user = mock(User.class);
        when(userService.processOAuthPostLogin(any(), eq(accessToken)))
            .thenReturn(user);

        return accessToken;
    }

    private static String mockAccessToken(OAuth2AuthorizedClient auth2User) {
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(auth2User.getAccessToken())
            .thenReturn(oAuth2AccessToken);

        String accessToken = Randoms.alpha();
        when(oAuth2AccessToken.getTokenValue()).thenReturn(accessToken);
        return accessToken;
    }

    private String setUpJwtTokenProvider() {
        String bearerToken = Randoms.alpha();
        when(jwtTokenProvider.createToken(any(Long.class))).thenReturn(bearerToken);
        return bearerToken;
    }
}