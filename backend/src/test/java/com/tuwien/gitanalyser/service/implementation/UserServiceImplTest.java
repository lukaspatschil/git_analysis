package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.entity.utils.UserFingerprintPair;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import com.tuwien.gitanalyser.security.jwt.FingerprintService;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.security.oauth2.BasicAuth2User;
import com.tuwien.gitanalyser.security.oauth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.oauth2.GitLabOAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import utils.Randoms;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = -1L;
    private UserService sut;
    private UserRepository userRepository;
    private FingerprintService fingerprintService;
    private JWTTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        fingerprintService = mock(FingerprintService.class);
        jwtTokenProvider = mock(JWTTokenProvider.class);
        sut = new UserServiceImpl(userRepository, fingerprintService, jwtTokenProvider);
    }

    @Test
    public void getById_existingId_returnsUser() throws NotFoundException {
        // Given
        User expectedUser = new User(EXISTING_USER_ID,
                                     "John Doe",
                                     "john.doe@github.com",
                                     Randoms.alpha(),
                                     AuthenticationProvider.GITHUB,
                                     Randoms.integer(),
                                     Randoms.alpha(),
                                     Randoms.alpha());

        when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(expectedUser));

        // When
        User result = sut.getUser(EXISTING_USER_ID);

        // Then
        assertThat(result, equalTo(expectedUser));
    }

    @Test
    public void getById_notExistingId_throwsNotFoundException() {
        // Given

        when(userRepository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> sut.getUser(NON_EXISTING_USER_ID));
    }

    @Test
    public void refreshAccessToken_randomRefreshToken_throwsAuthenticationException() {
        // Given
        String refreshToken = Randoms.alpha();
        String fingerprint = Randoms.alpha();

        when(jwtTokenProvider.getUserId(refreshToken)).thenThrow(AuthenticationException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.refreshAccessToken(refreshToken, fingerprint));
    }

    @Test
    public void refreshAccessToken_userIdDoesNotExist_throwsAuthenticationException() {
        // Given
        String refreshToken = Randoms.alpha();
        String fingerprint = Randoms.alpha();
        Long userId = Randoms.getLong();

        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.refreshAccessToken(refreshToken, fingerprint));
    }

    @Test
    public void refreshAccessToken_hashDoesNotMatch_throwsAuthenticationException() {
        // Given
        String refreshToken = Randoms.alpha();
        String fingerprint = Randoms.alpha();
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getFingerPrintHash()).thenReturn(Randoms.alpha());

        when(fingerprintService.sha256(fingerprint)).thenReturn(Randoms.alpha());

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.refreshAccessToken(refreshToken, fingerprint));
    }

    @Test
    public void refreshAccessToken_hashDoesMatch_saveUserWithNewFingerPrint() {
        // Given
        String refreshToken = Randoms.alpha();
        String fingerprint = Randoms.alpha();
        String fingerprintHashed = Randoms.alpha();
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        FingerprintPair newFingerprintPair = new FingerprintPair(Randoms.alpha(), Randoms.alpha());

        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getFingerPrintHash()).thenReturn(fingerprintHashed);

        when(fingerprintService.sha256(fingerprint)).thenReturn(fingerprintHashed);

        when(fingerprintService.createFingerprint()).thenReturn(newFingerprintPair);

        // When
        sut.refreshAccessToken(refreshToken, fingerprint);

        // Then
        verify(userRepository).save(user);
    }

    @Test
    public void refreshAccessToken_hashDoesMatch_returnsCorrectData() {
        // Given
        String refreshToken = Randoms.alpha();
        String fingerprint = Randoms.alpha();
        String fingerprintHashed = Randoms.alpha();
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String newAccessToken = Randoms.alpha();
        String newRefreshToken = Randoms.alpha();
        FingerprintPair newFingerprintPair = new FingerprintPair(Randoms.alpha(), Randoms.alpha());

        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getFingerPrintHash()).thenReturn(fingerprintHashed);

        when(fingerprintService.sha256(fingerprint)).thenReturn(fingerprintHashed);

        when(jwtTokenProvider.createAccessToken(userId)).thenReturn(newAccessToken);
        when(jwtTokenProvider.createRefreshToken(userId)).thenReturn(newRefreshToken);

        when(fingerprintService.createFingerprint()).thenReturn(newFingerprintPair);

        // When
        RefreshAuthenticationInternalDTO result = sut.refreshAccessToken(refreshToken, fingerprint);

        // Then
        assertThat(result, allOf(
            hasFeature("accessToken", RefreshAuthenticationInternalDTO::getAccessToken, equalTo(newAccessToken)),
            hasFeature("refreshToken", RefreshAuthenticationInternalDTO::getRefreshToken, equalTo(newRefreshToken)),
            hasFeature("fingerprint", RefreshAuthenticationInternalDTO::getFingerprint, equalTo(newFingerprintPair.getFingerprint()))
        ));
    }

    @ParameterizedTest
    @EnumSource(AuthenticationProvider.class)
    public void processOAuthPostLogin_nonExistingUser_createsNewUser(AuthenticationProvider authenticationProvider) {
        // Given
        int platformId = Randoms.integer();
        String username = "John Doe";
        User expectedUser = createUser(authenticationProvider, platformId, username);

        when(userRepository.findByAuthenticationProviderAndPlatformId(authenticationProvider, platformId)).thenReturn(
            Optional.empty());

        // When
        sut.processOAuthPostLogin(createAuth2User(authenticationProvider, platformId, username), null);

        // Then
        verify(userRepository).save(expectedUser);
    }

    @ParameterizedTest
    @EnumSource(AuthenticationProvider.class)
    public void processOAuthPostLogin_userExists_returnsUser(AuthenticationProvider authenticationProvider) {
        // Given
        int platformId = Randoms.integer();
        String username = "John Doe";
        User expectedUser = createUser(authenticationProvider, platformId, username);

        when(userRepository.findByAuthenticationProviderAndPlatformId(authenticationProvider, platformId))
            .thenReturn(Optional.of(expectedUser));

        // When
        UserFingerprintPair userFingerprintPair = sut.processOAuthPostLogin(createAuth2User(authenticationProvider,
                                                                                            platformId,
                                                                                            username), null);

        // Then
        assertThat(userFingerprintPair.getUser(), equalTo(expectedUser));
    }

    private BasicAuth2User createAuth2User(AuthenticationProvider authenticationProvider, int platformId,
                                           String username) {
        BasicAuth2User auth2User;

        if (authenticationProvider.equals(AuthenticationProvider.GITHUB)) {
            auth2User = mock(GitHubOAuth2User.class);
            when(auth2User.getAuthenticationProvider()).thenReturn(AuthenticationProvider.GITHUB);
        } else {
            auth2User = mock(GitLabOAuth2User.class);
            when(auth2User.getAuthenticationProvider()).thenReturn(AuthenticationProvider.GITLAB);
        }

        when(auth2User.getPlatformId()).thenReturn(platformId);
        when(auth2User.getName()).thenReturn(username);

        return auth2User;

    }

    private User createUser(AuthenticationProvider authenticationProvider, int platformId, String username) {
        FingerprintPair fingerprint = mockFingerprint();

        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPlatformId(platformId);
        expectedUser.setFingerPrintHash(fingerprint.getHash());
        expectedUser.setAuthenticationProvider(authenticationProvider);
        return expectedUser;
    }

    private FingerprintPair mockFingerprint() {
        FingerprintPair fingerprint = new FingerprintPair(Randoms.alpha(), Randoms.alpha());
        when(this.fingerprintService.createFingerprint()).thenReturn(fingerprint);
        return fingerprint;
    }
}