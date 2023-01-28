package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.UserService;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryServiceImplTest extends ServiceImplementationBaseTest {

    private RepositoryServiceImpl sut;
    private UserService userService;
    private GitHubAPI gitHubAPI;
    private GitLabAPI gitLabAPI;
    private String exceptionString;

    @BeforeEach
    void setUp() {
        gitHubAPI = mock(GitHubAPI.class);
        gitLabAPI = mock(GitLabAPI.class);
        userService = mock(UserService.class);
        sut = new RepositoryServiceImpl(userService, gitHubAPI, gitLabAPI);
        exceptionString = "testException";
    }

    @Test
    void getAllRepositories_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        when(gitLabAPI.getAllRepositories(accessToken)).thenThrow(
            new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_GitHubAuthorizationThrowsException_shouldThrowException()
        throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        when(gitHubAPI.getAllRepositories(accessToken)).thenThrow(
            new IOException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_randomAuthorization_shouldThrowException() {
        // Given

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(Randoms.getLong()));
    }

    @Test
    void getRepositoryById_GitLabAuthorization_shouldCallGitLabAPI() throws GitLabApiException, NotFoundException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitLabAPI).getRepositoryById(accessToken, repositoryId);
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitHubAPI).getRepositoryById(accessToken, repositoryId);
    }

    private String prepareUserService(Long userId, User user, AuthenticationProvider github) throws NotFoundException {
        String accessToken = Randoms.alpha();
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAccessToken()).thenReturn(accessToken);
        when(user.getAuthenticationProvider()).thenReturn(github);
        return accessToken;
    }

    @Test
    void getRepositoryById_randomAuthorization_shouldThrowException() throws NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = Randoms.alpha();
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAccessToken()).thenReturn(accessToken);

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getRepositoryById(userId, repositoryId));
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldReturnRepository() throws IOException, NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);
        Repository repository = new Repository(repositoryId, Randoms.alpha(), Randoms.alpha());

        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(repository);

        // When
        Repository result = sut.getRepositoryById(userId, repositoryId);

        // Then
        assertThat(result, equalTo(repository));
    }
}
