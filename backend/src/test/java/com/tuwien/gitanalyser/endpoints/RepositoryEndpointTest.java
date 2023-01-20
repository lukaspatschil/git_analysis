package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import utils.Randoms;

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
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        // When
        sut.getAllRepositories(authentication);

        // Then
        verify(repositoryService).getAllRepositories(Long.parseLong(authentication.getName()));
    }

    @Test
    void getAllRepositories_serviceReturnsListOfRepositories_returnList() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        when(repositoryService.getAllRepositories(Long.parseLong(authentication.getName())))
            .thenReturn(List.of(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3));

        // When
        List<RepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, containsInAnyOrder(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3));
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyListOfRepositories_returnList() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        when(repositoryService.getAllRepositories(Long.parseLong(authentication.getName())))
            .thenReturn(List.of());

        // When
        List<RepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, equalTo(List.of()));
    }

    @Test
    void getRepositoryById_always_shouldCallService() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        // When
        sut.getRepositoryById(authentication, 1L);

        // Then
        verify(repositoryService).getRepositoryById(Long.parseLong(authentication.getName()), 1L);
    }

    @Test
    void getRepositoryById_givenOneRepository_returnsRepository() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        when(repositoryService.getRepositoryById(Long.parseLong(authentication.getName()), 1L))
            .thenReturn(REPOSITORY_1);

        // When
        RepositoryDTO repository = sut.getRepositoryById(authentication, 1L);

        // Then
        assertThat(repository, equalTo(REPOSITORY_1));
    }
}
