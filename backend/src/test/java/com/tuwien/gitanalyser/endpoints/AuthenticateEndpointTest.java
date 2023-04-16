package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.RefreshAccessTokenDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RefreshAuthenticationDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

class AuthenticateEndpointTest {

    private AuthenticateEndpoint sut;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        sut = new AuthenticateEndpoint(userService);
    }

    @Test
    void refreshToken_shouldCallUserEndpoint() {
        // Given
        String refreshToken = Randoms.alpha();
        RefreshAccessTokenDTO refreshAccessTokenDTO = new RefreshAccessTokenDTO(refreshToken);
        String fingerprint = Randoms.alpha();
        HttpServletResponse response = mock(HttpServletResponse.class);

        prepareUserServiceRefreshAccessToken(refreshToken, fingerprint);

        // When
        sut.refreshToken(refreshAccessTokenDTO, fingerprint, response);

        // Then
        verify(userService).refreshAccessToken(refreshAccessTokenDTO.getRefreshToken(), fingerprint);
    }

    @Test
    void refreshToken_userServiceReturnsDTO_shouldReturnCorrectData() {
        // Given
        String refreshToken = Randoms.alpha();
        RefreshAccessTokenDTO refreshAccessTokenDTO = new RefreshAccessTokenDTO(refreshToken);
        String fingerprint = Randoms.alpha();
        HttpServletResponse response = mock(HttpServletResponse.class);

        RefreshAuthenticationInternalDTO refreshAuthenticationInternalDTO = prepareUserServiceRefreshAccessToken(
            refreshToken,
            fingerprint);

        // When
        RefreshAuthenticationDTO result = sut.refreshToken(refreshAccessTokenDTO,
                                                           fingerprint,
                                                           response);

        // Then
        assertThat(result, allOf(
            hasFeature("accessToken", RefreshAuthenticationDTO::getAccessToken,
                       equalTo(refreshAuthenticationInternalDTO.getAccessToken())),
            hasFeature("refreshToken", RefreshAuthenticationDTO::getRefreshToken,
                       equalTo(refreshAuthenticationInternalDTO.getRefreshToken()))
        ));
    }

    @Test
    void refreshToken_userServiceReturnsDTO_shouldSetCookie() {
        // Given
        String refreshToken = Randoms.alpha();
        RefreshAccessTokenDTO refreshAccessTokenDTO = new RefreshAccessTokenDTO(refreshToken);
        String fingerprint = Randoms.alpha();
        HttpServletResponse response = mock(HttpServletResponse.class);

        RefreshAuthenticationInternalDTO refreshAuthenticationInternalDTO = prepareUserServiceRefreshAccessToken(
            refreshToken,
            fingerprint);

        // When
        sut.refreshToken(refreshAccessTokenDTO,
                         fingerprint,
                         response);

        // Then
        verify(response).addCookie(
            argThat(allOf(hasFeature("fingerprintName", Cookie::getName,
                                     equalTo("fingerprint")),
                          hasFeature("fingerprintValue", Cookie::getValue,
                                     equalTo(refreshAuthenticationInternalDTO.getFingerprint()))
            ))
        );
    }

    @Test
    void refreshToken_userServiceThrowsAuthenticationException_shouldThrowAuthenticationException() {
        // Given
        String refreshToken = Randoms.alpha();
        RefreshAccessTokenDTO refreshAccessTokenDTO = new RefreshAccessTokenDTO(refreshToken);
        String fingerprint = Randoms.alpha();
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userService.refreshAccessToken(refreshToken, fingerprint)).thenThrow(AuthenticationException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.refreshToken(refreshAccessTokenDTO,
                                                                           fingerprint,
                                                                           response));

    }

    private RefreshAuthenticationInternalDTO prepareUserServiceRefreshAccessToken(String refreshToken,
                                                                                  String fingerprint) {
        RefreshAuthenticationInternalDTO refreshAuthenticationInternalDTO = new RefreshAuthenticationInternalDTO(
            Randoms.alpha(),
            Randoms.alpha(),
            Randoms.alpha());
        when(userService.refreshAccessToken(refreshToken, fingerprint)).thenReturn(refreshAuthenticationInternalDTO);
        return refreshAuthenticationInternalDTO;
    }
}