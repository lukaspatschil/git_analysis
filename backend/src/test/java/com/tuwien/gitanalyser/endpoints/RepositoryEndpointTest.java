package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RepositoryEndpointTest {

    private RepositoryService repositoryService;
    private RepositoryEndpoint sut;

    @BeforeEach
    void setUp() {
        repositoryService = mock(RepositoryService.class);
        sut = new RepositoryEndpoint(repositoryService);
    }

    @Test
    void getAllRepositories_always_shouldCallService() {

        // Given

        // When
        sut.getAllRepositories(mock(OAuth2AuthorizedClient.class));

        // Then
        verify(repositoryService).getAllRepositories(mock(OAuth2AuthorizedClient.class));
    }
}