package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitHubOAuth2User;
import com.tuwien.gitanalyser.security.OAuth2.GitLabOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import utils.Randoms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest extends ServiceImplementationBaseTest {

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = -1L;
    private static final User USER_1 = mock(User.class);
    private static final User USER_2 = mock(User.class);
    private static final User USER_3 = mock(User.class);
    private UserServiceImpl sut;
    private UserRepository userRepository;

    private static User createUser(AuthenticationProvider authenticationProvider, int platformId, String username) {
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPlatformId(platformId);
        expectedUser.setAuthenticationProvider(authenticationProvider);
        return expectedUser;
    }

    private static BasicAuth2User createAuth2User(AuthenticationProvider authenticationProvider, int platformId,
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

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        sut = new UserServiceImpl(userRepository);
    }

    @Test
    public void getById_existingId_returnsUser() {
        // Given
        User expectedUser = new User(EXISTING_USER_ID, "John Doe", "john.doe@github.com", AuthenticationProvider.GITHUB,
                                     Randoms.integer(), Randoms.alpha(), Randoms.alpha());

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
    public void getAll_noUsersInDB_returnsEmptyArray() {
        // Given

        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<User> users = sut.getAll();

        // Then
        assertThat(users.size(), equalTo(0));
    }

    @Test
    public void getAll_threeUsersInDB_returnsAllThreeUsers() {
        // Given
        List<User> allUsers = List.of(USER_1, USER_2, USER_3);

        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        List<User> users = sut.getAll();

        // Then
        assertThat(users, containsInAnyOrder(USER_1, USER_2, USER_3));
    }

    @ParameterizedTest
    @EnumSource(AuthenticationProvider.class)
    public void processOAuthPostLogin_nonExistingUser_createsNewUser(AuthenticationProvider authenticationProvider) {
        // Given
        int platformId = 1;
        String username = "John Doe";
        User expectedUser = createUser(authenticationProvider, platformId, username);

        when(userRepository.findByPlatformAndPlatformId(authenticationProvider, platformId)).thenReturn(List.of());

        // When
        sut.processOAuthPostLogin(createAuth2User(authenticationProvider, platformId, username));

        // Then
        verify(userRepository).save(expectedUser);
    }
}