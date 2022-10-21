package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.security.ClientRegistrations;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import utils.Randoms;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryServiceImplTest extends ServiceImplementationBaseTest {

    private static final int ONE_HOUR_IN_SECONDS = 3600;
    private RepositoryServiceImpl sut;
    private GitHubAPI gitHubAPI;
    private GitLabAPI gitLabAPI;
    private OAuth2AccessToken accessToken;
    private String exceptionString;

    @BeforeEach
    void setUp() {
        gitHubAPI = mock(GitHubAPI.class);
        gitLabAPI = mock(GitLabAPI.class);
        sut = new RepositoryServiceImpl(gitHubAPI, gitLabAPI);
        accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, Randoms.alpha(), Instant.now(),
                                            Instant.now().plusSeconds(ONE_HOUR_IN_SECONDS));
        exceptionString = "testException";
    }

    @Test
    void getAllRepositories_GitLabAuthorization_shouldCallGitLabAPI() throws GitLabApiException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitLabClientRegistration(),
                                                                   Randoms.alpha(), accessToken);

        // When
        sut.getAllRepositories(client);

        // Then
        verify(gitLabAPI).getAllRepositories(client.getAccessToken().getTokenValue());
    }

    @Test
    void getAllRepositories_GitHubAuthorization_shouldCallGitHubAPI() throws IOException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitHubClientRegistration(),
                                                                   Randoms.alpha(), accessToken);

        // When
        sut.getAllRepositories(client);

        // Then
        verify(gitHubAPI).getAllRepositories(client.getAccessToken().getTokenValue());
    }

    @Test
    void getAllRepositories_GitLabAuthorizationThrowsException_shouldThrowRuntimeException() throws GitLabApiException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitLabClientRegistration(),
                                                                   Randoms.alpha(), accessToken);

        when(gitLabAPI.getAllRepositories(client.getAccessToken().getTokenValue())).thenThrow(
            new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(client), exceptionString);
    }

    @Test
    void getAllRepositories_GitHubAuthorizationThrowsException_shouldThrowException() throws IOException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitHubClientRegistration(),
                                                                   Randoms.alpha(), accessToken);
        when(gitHubAPI.getAllRepositories(client.getAccessToken().getTokenValue())).thenThrow(
            new IOException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(client), exceptionString);
    }

    @Test
    void getAllRepositories_randomAuthorization_shouldThrowException() {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(getRandomAuthorization(), Randoms.alpha(),
                                                                   accessToken);

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(client));
    }

    @Test
    void getRepositoryById_GitLabAuthorization_shouldCallGitLabAPI() throws GitLabApiException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitLabClientRegistration(),
                                                                   Randoms.alpha(), accessToken);
        long id = Randoms.getLong();

        // When
        sut.getRepositoryById(client, id);

        // Then
        verify(gitLabAPI).getRepositoryById(client.getAccessToken().getTokenValue(), id);
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldCallGitHubAPI() throws IOException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitHubClientRegistration(),
                                                                   Randoms.alpha(), accessToken);
        long id = Randoms.getLong();

        // When
        sut.getRepositoryById(client, id);

        // Then
        verify(gitHubAPI).getRepositoryById(client.getAccessToken().getTokenValue(), id);
    }

    @Test
    void getRepositoryById_randomAuthorization_shouldThrowException() {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(getRandomAuthorization(), Randoms.alpha(),
                                                                   accessToken);
        long id = Randoms.getLong();

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getRepositoryById(client, id));
    }
}
