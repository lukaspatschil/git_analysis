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

class RepositoryServiceImplTest extends ServiceImplementationBaseTest{

    private RepositoryServiceImpl sut;
    private GitHubAPI gitHubAPI;
    private GitLabAPI gitLabAPI;
    private OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() {
        gitHubAPI = mock(GitHubAPI.class);
        gitLabAPI = mock(GitLabAPI.class);
        sut = new RepositoryServiceImpl(gitHubAPI, gitLabAPI);
        accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, Randoms.alpha(), Instant.now(),
                                            Instant.now().plusSeconds(3600));
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
    void getAllRepositories_GitHubAuthorization_shouldCallGitLabAPI() throws IOException {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(ClientRegistrations.gitHubClientRegistration(),
                                                                   Randoms.alpha(), accessToken);

        // When
        sut.getAllRepositories(client);

        // Then
        verify(gitHubAPI).getAllRepositories(client.getAccessToken().getTokenValue());
    }

    @Test
    void getAllRepositories_randomAuthorization_shouldThrowException() {
        // Given
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(getRandomAuthorization(), Randoms.alpha(),
                                                                   accessToken);

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(client));
    }
}