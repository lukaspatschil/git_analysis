package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitHubException;
import com.tuwien.gitanalyser.exception.GitLabException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitHubExceptionHandlerServiceImplTest {

    private GitHubExceptionHandlerServiceImpl sut;
    private GitHubAccessTokenServiceImpl gitHubAccessTokenService;

    @BeforeEach
    void setUp() {
        gitHubAccessTokenService = mock(GitHubAccessTokenServiceImpl.class);
        sut = new GitHubExceptionHandlerServiceImpl(gitHubAccessTokenService);
    }

    @Test
    void getAllRepositories_userIdGiven_callsService() throws GitException {
        // Given
        long userId = Randoms.getLong();

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAccessTokenService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();

        when(gitHubAccessTokenService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllRepositories_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement() throws GitException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        when(gitHubAccessTokenService.getAllRepositories(userId)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository3 = mock(NotSavedRepositoryInternalDTO.class);

        when(gitHubAccessTokenService.getAllRepositories(userId))
            .thenReturn(List.of(repository1, repository2, repository3));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(repository1, repository2, repository3));
    }

    @Test
    void getAllRepositories_serviceThrowsGitHubException_shouldThrowGitHubException()
        throws GitException {
        // Given
        long userId = Randoms.getLong();

        when(gitHubAccessTokenService.getAllRepositories(userId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllRepositories(userId));
    }

    @Test
    void getAllBranches_userIdGiven_callsService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When
        sut.getAllBranches(userId, platformId);

        // Then
        verify(gitHubAccessTokenService).getAllBranches(userId, platformId);
    }

    @Test
    void getAllBranches_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitHubAccessTokenService.getAllBranches(userId, platformId)).thenReturn(List.of());

        // When
        var result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllBranches_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        BranchInternalDTO branch1 = mock(BranchInternalDTO.class);

        when(gitHubAccessTokenService.getAllBranches(userId, platformId)).thenReturn(List.of(branch1));

        // When
        var result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(branch1));
    }

    @Test
    void getAllBranches_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        BranchInternalDTO branch1 = mock(BranchInternalDTO.class);
        BranchInternalDTO branch2 = mock(BranchInternalDTO.class);
        BranchInternalDTO branch3 = mock(BranchInternalDTO.class);

        when(gitHubAccessTokenService.getAllBranches(userId, platformId))
            .thenReturn(List.of(branch1, branch2, branch3));

        // When
        List<BranchInternalDTO> result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(branch1, branch2, branch3));
    }

    @Test
    void getAllBranches_serviceThrowsGitHubException_shouldThrowGitHubException() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitHubAccessTokenService.getAllBranches(userId, platformId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllBranches(userId, platformId));
    }

    @Test
    void getRepositoryById_userIdGiven_callsService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When
        sut.getRepositoryById(userId, platformId);

        // Then
        verify(gitHubAccessTokenService).getRepositoryById(userId, platformId);
    }

    @Test
    void getRepositoryById_serviceReturnsRepository_shouldReturnRepository() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        NotSavedRepositoryInternalDTO repository = mock(NotSavedRepositoryInternalDTO.class);

        when(gitHubAccessTokenService.getRepositoryById(userId, platformId)).thenReturn(repository);

        // When
        var result = sut.getRepositoryById(userId, platformId);

        // Then
        assertThat(result, equalTo(repository));
    }

    @Test
    void getRepositoryById_serviceThrowsGitHubException_shouldThrowGitHubException()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitHubAccessTokenService.getRepositoryById(userId, platformId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getRepositoryById(userId, platformId));
    }

    @Test
    void getAllCommits_userIdGiven_callsService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        // When
        sut.getAllCommits(userId, platformId, branch);

        // Then
        verify(gitHubAccessTokenService).getAllCommits(userId, platformId, branch);
    }

    @Test
    void getAllCommits_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitHubAccessTokenService.getAllCommits(userId, platformId, branch)).thenReturn(List.of());

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllCommits_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);

        when(gitHubAccessTokenService.getAllCommits(userId, platformId, branch)).thenReturn(List.of(commit1));

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(commit1));
    }

    @Test
    void getAllCommits_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit3 = mock(CommitInternalDTO.class);

        when(gitHubAccessTokenService.getAllCommits(userId, platformId, branch))
            .thenReturn(List.of(commit1, commit2, commit3));

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result.size(), equalTo(3));
        MatcherAssert.assertThat(result, containsInAnyOrder(commit1, commit2, commit3));
    }

    @Test
    void getAllCommits_serviceThrowsGitHubException_shouldThrowGitHubException()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitHubAccessTokenService.getAllCommits(userId, platformId, branch)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllCommits(userId, platformId, branch));
    }

    @Test
    void repositoryAccessibleByUser_userIdGivenServiceReturnsTrue_shouldReturnTrue() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(true));
    }

    @Test
    void repositoryAccessibleByUser_serviceReturnsElement_shouldReturnTrue()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitHubAccessTokenService.getRepositoryById(userId, platformId))
            .thenReturn(mock(NotSavedRepositoryInternalDTO.class));

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(true));
    }

    @Test
    void repositoryAccessibleByUser_serviceThrowsGitLabException_shouldReturnFalse()
        throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitHubAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(GitLabException.class);

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(false));
    }

    @Test
    void getEmail_always_shouldCallService() throws GitException {
        // Given
        long userId = Randoms.getLong();

        // When
        sut.getEmail(userId);

        // Then
        verify(gitHubAccessTokenService).getEmail(userId);
    }

    @Test
    void getEmail_accessTokenServiceReturnsString_shouldReturnString() throws GitException {
        // Given
        long userId = Randoms.getLong();
        String expectedEmail = Randoms.alpha();
        when(gitHubAccessTokenService.getEmail(userId)).thenReturn(expectedEmail);

        // When
        String result = sut.getEmail(userId);

        // Then
        assertThat(result, equalTo(expectedEmail));
    }
}