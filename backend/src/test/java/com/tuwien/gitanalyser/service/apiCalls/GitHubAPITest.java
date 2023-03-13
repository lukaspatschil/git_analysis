package com.tuwien.gitanalyser.service.apiCalls;

import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.service.apiCalls.factory.GitHubAPIFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import utils.Randoms;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubAPITest {

    private static final String TEST_EXCEPTION_ERROR_MESSAGE = "testException";
    private GitHubAPI sut;
    private GitHubAPIFactory gitHubAPIFactory;
    private String accessToken;

    private GHRepository firstRepository;
    private GHRepository secondRepository;

    private static GHMyself mockMySelf(GitHub gitHub) throws IOException {
        GHMyself ghMyself = mock(GHMyself.class);
        when(gitHub.getMyself()).thenReturn(ghMyself);
        return ghMyself;
    }

    @BeforeEach
    void setUp() {
        gitHubAPIFactory = mock(GitHubAPIFactory.class);
        sut = new GitHubAPI(gitHubAPIFactory);

        accessToken = Randoms.alpha();

        firstRepository = mockRepository(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
        secondRepository = mockRepository(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    }

    @Test
    void getAllRepositories_noRepositoriesAvailable_returnEmptyList() throws IOException {
        // Given
        GitHub gitHub = mockFactory();
        GHMyself ghMyself = mockMySelf(gitHub);
        when(ghMyself.getAllRepositories()).thenReturn(Map.of());

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, is(empty()));
    }

    @Disabled
    @Test
    void getAllRepositories_OneProject_singleItemAsList() throws IOException {
        // Given
        GitHub gitHub = mockFactory();
        GHMyself ghMyself = mockMySelf(gitHub);
        when(ghMyself.getAllRepositories()).thenReturn(Map.of(Randoms.alpha(), firstRepository));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("id", is(1L))
        ));
    }

    @Disabled
    @Test
    void getAllRepositories_twoProjects_return2ItemsList() throws IOException {
        // Given
        GitHub gitHub = mockFactory();
        GHMyself ghMyself = mockMySelf(gitHub);
        when(ghMyself.getAllRepositories()).thenReturn(Map.of(Randoms.alpha(), firstRepository,
                                                              Randoms.alpha(), secondRepository));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("id", is(1L)),
            hasProperty("id", is(2L))
        ));
    }

    @Test
    void getAllRepositories_gitHubLibraryThrowsIOException_throwIOException() throws IOException {
        // Given
        GitHub gitHub = mockFactory();
        GHMyself ghMyself = mockMySelf(gitHub);
        when(ghMyself.getAllRepositories()).thenThrow(new IOException(TEST_EXCEPTION_ERROR_MESSAGE));

        // When + Then
        IOException exception = assertThrows(IOException.class, () -> sut.getAllRepositories(accessToken));
        assertThat(exception.getMessage(), is(TEST_EXCEPTION_ERROR_MESSAGE));
    }

    @Test
    void getRepositoryById_gitHubLibraryThrowsException_sutThrowsException() throws IOException {
        // Given
        long exceptionRepositoryId = Randoms.getLong();
        GitHub gitHub = mockFactory();
        when(gitHub.getRepositoryById(exceptionRepositoryId)).thenThrow(new IOException(TEST_EXCEPTION_ERROR_MESSAGE));

        // When + Then
        IOException exception = assertThrows(IOException.class,
                                             () -> sut.getRepositoryById(accessToken, exceptionRepositoryId));
        assertThat(exception.getMessage(), is(TEST_EXCEPTION_ERROR_MESSAGE));
    }

    @Disabled
    @Test
    void getRepositoryById_gitlabLibraryReturnsOneRepository_sutReturnsRepository() throws IOException {
        // Given
        long queryRepositoryId = firstRepository.getId();

        GitHub gitHub = mockFactory();
        when(gitHub.getRepositoryById(queryRepositoryId)).thenReturn(firstRepository);

        // When
        NotSavedRepositoryInternalDTO result = sut.getRepositoryById(accessToken, queryRepositoryId);

        // Then
        assertThat(result, hasProperty("id", is(queryRepositoryId)));
    }

    @Test
    void getAllBranches_noBranchAvailable_returnEmptyList() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();

        GitHub gitHub = mockFactory();
        GHRepository ghRepository = mock(GHRepository.class);
        when(gitHub.getRepositoryById(repositoryId)).thenReturn(ghRepository);
        when(ghRepository.getBranches()).thenReturn(Map.of());

        // When
        var result = sut.getAllBranches(accessToken, repositoryId);

        // Then
        assertThat(result, is(empty()));
    }


    private GitHub mockFactory() throws IOException {
        GitHub github = mock(GitHub.class);
        when(gitHubAPIFactory.createObject(accessToken)).thenReturn(github);
        return github;
    }

    private GHRepository mockRepository(long id, String name, String httpTransportUrl) {
        GHRepository repository = mock(GHRepository.class);
        // when(repository.getId()).thenReturn(id);
        when(repository.getName()).thenReturn(name);
        when(repository.getHttpTransportUrl()).thenReturn(httpTransportUrl);
        return repository;
    }
}