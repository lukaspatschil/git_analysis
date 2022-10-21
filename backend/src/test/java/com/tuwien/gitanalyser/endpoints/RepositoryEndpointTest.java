package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryEndpointTest {

    private static final RepositoryDTO REPOSITORY_1 = mock(RepositoryDTO.class);
    private static final RepositoryDTO REPOSITORY_2 = mock(RepositoryDTO.class);
    private static final RepositoryDTO REPOSITORY_3 = mock(RepositoryDTO.class);
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
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        // When
        sut.getAllRepositories(authorizedClient);

        // Then
        verify(repositoryService).getAllRepositories(authorizedClient);
    }

    @Test
    void getAllRepositories_serviceReturnsListOfRepositories_returnList() {
        // Given
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        when(repositoryService.getAllRepositories(authorizedClient))
            .thenReturn(List.of(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3));

        // When
        List<RepositoryDTO> repositoryList = sut.getAllRepositories(authorizedClient);

        // Then
        assertThat(repositoryList, containsInAnyOrder(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3));
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyListOfRepositories_returnList() {
        // Given
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        when(repositoryService.getAllRepositories(authorizedClient))
            .thenReturn(List.of());

        // When
        List<RepositoryDTO> repositoryList = sut.getAllRepositories(authorizedClient);

        // Then
        assertThat(repositoryList, equalTo(List.of()));
    }

    @Test
    void getRepositoryById_always_shouldCallService() {
        // Given
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        // When
        sut.getRepositoryById(authorizedClient, 1L);

        // Then
        verify(repositoryService).getRepositoryById(authorizedClient, 1L);
    }

    @Test
    void getRepositoryById_givenOneRepository_returnsRepository() {
        // Given
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        when(repositoryService.getRepositoryById(authorizedClient, 1L))
            .thenReturn(REPOSITORY_1);

        // When
        RepositoryDTO repository = sut.getRepositoryById(authorizedClient, 1L);

        // Then
        assertThat(repository, equalTo(REPOSITORY_1));
    }
}
