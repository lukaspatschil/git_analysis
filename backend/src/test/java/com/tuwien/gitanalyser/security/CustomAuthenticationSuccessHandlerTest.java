package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.UserFingerprintPair;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.security.oauth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.oauth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.apache.catalina.connector.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import utils.Randoms;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAuthenticationSuccessHandlerTest {

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
        String bearerToken = setUpJwtAccessTokenProvider();
        String refreshToken = setUpJwtRefreshTokenProvider();
        prepareAuthorizedClientRepository(gitLabAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitLabAuthentication);

        // Then
        verify(response).sendRedirect(
            AuthenticationConstants.FRONTEND_REDIRECT_AFTER_LOGIN_URL + "#" +
                "accessToken=" + bearerToken + "&" +
                "refreshToken=" + refreshToken);
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_redirectsToStartPage() throws IOException {
        // Given
        String bearerToken = setUpJwtAccessTokenProvider();
        String refreshToken = setUpJwtRefreshTokenProvider();
        prepareAuthorizedClientRepository(gitHubAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(response).sendRedirect(
            AuthenticationConstants.FRONTEND_REDIRECT_AFTER_LOGIN_URL + "#" + "accessToken=" + bearerToken + "&" +
                "refreshToken=" + refreshToken);
    }

    @Test
    public void onAuthenticationSuccess_gitHubAuthentication_shouldCallProcessOAuthPostLogin() throws IOException {
        // Given
        var tokens = prepareAuthorizedClientRepository(gitHubAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitHubAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any(GitHubOAuth2User.class),
                                                  eq(tokens.getAccessToken()), eq(tokens.getRefreshToken()));
    }

    @Test
    public void onAuthenticationSuccess_gitLabAuthentication_shouldSetFingerprintCookie() throws IOException {
        // Given
        Tokens tokens = prepareAuthorizedClientRepository(gitLabAuthentication);

        // When
        sut.onAuthenticationSuccess(request, response, gitLabAuthentication);

        // Then
        verify(userService).processOAuthPostLogin(any(GitLabOAuth2User.class),
                                                  eq(tokens.getAccessToken()), eq(tokens.getRefreshToken()));
    }

    private Tokens prepareAuthorizedClientRepository(OAuth2AuthenticationToken authenticationProvider) {
        OAuth2AuthorizedClient auth2User = mock(OAuth2AuthorizedClient.class);

        when(authorizedClientRepository.loadAuthorizedClient(authenticationProvider.getAuthorizedClientRegistrationId(),
                                                             authenticationProvider,
                                                             request))
            .thenReturn(auth2User);

        Tokens tokens = mockTokens(auth2User);

        User user = mock(User.class);
        FingerprintPair fingerprintPair = mock(FingerprintPair.class);
        when(userService.processOAuthPostLogin(any(),
                                               eq(tokens.getAccessToken()),
                                               eq(tokens.getRefreshToken())))
            .thenReturn(new UserFingerprintPair(user, fingerprintPair));

        return tokens;
    }

    private String setUpJwtAccessTokenProvider() {
        String bearerToken = Randoms.alpha();
        when(jwtTokenProvider.createAccessToken(any(Long.class))).thenReturn(bearerToken);
        return bearerToken;
    }

    private String setUpJwtRefreshTokenProvider() {
        String refreshToken = Randoms.alpha();
        when(jwtTokenProvider.createRefreshToken(any(Long.class))).thenReturn(refreshToken);
        return refreshToken;
    }

    private Tokens mockTokens(OAuth2AuthorizedClient auth2User) {

        String accessToken = "accessToken";
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(auth2User.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(accessToken);

        OAuth2RefreshToken oAuth2RefreshToken = mock(OAuth2RefreshToken.class);
        when(auth2User.getRefreshToken()).thenReturn(oAuth2RefreshToken);
        String refreshToken = "refreshToken";
        when(oAuth2RefreshToken.getTokenValue()).thenReturn(refreshToken);
        return new Tokens(accessToken, refreshToken);
    }

    class Tokens {
        private final String accessToken;
        private final String refreshToken;

        public Tokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

    }
}