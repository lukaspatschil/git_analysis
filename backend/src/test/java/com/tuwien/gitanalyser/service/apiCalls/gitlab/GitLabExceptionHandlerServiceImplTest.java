package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitLabExceptionHandlerServiceImplTest {

    private GitLabExceptionHandlerServiceImpl sut;
    private GitLabAccessTokenServiceImpl gitLabAccessTokenService;
    private GitLabRefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        gitLabAccessTokenService = mock(GitLabAccessTokenServiceImpl.class);
        refreshTokenService = mock(GitLabRefreshTokenService.class);
        sut = new GitLabExceptionHandlerServiceImpl(gitLabAccessTokenService, refreshTokenService);
    }

    @Test
    void getAllRepositories_userIdGiven_callsService() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabAccessTokenService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyList_shouldEmptyList() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllRepositories_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        when(gitLabAccessTokenService.getAllRepositories(userId)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository3 = mock(NotSavedRepositoryInternalDTO.class);

        when(gitLabAccessTokenService.getAllRepositories(userId))
            .thenReturn(List.of(repository1, repository2, repository3));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(repository1, repository2, repository3));
    }

    @Test
    void getAllRepositories_serviceThrowsGitLabException_shouldThrowGitLabException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllRepositories(userId)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllRepositories(userId));
    }

    @Test
    void getAllRepositories_serviceThrowsTryRefreshException_shouldCallRefreshService()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllRepositories(userId)).thenThrow(TryRefreshException.class)
                                                                 .thenReturn(emptyList());

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(refreshTokenService).refreshGitAccessToken(userId);
    }

    @Test
    void getAllRepositories_serviceThrowsTryRefreshException_shouldCallServiceTwice()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllRepositories(userId)).thenThrow(TryRefreshException.class)
                                                                 .thenReturn(emptyList());

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabAccessTokenService, times(2)).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_serviceThrowsTryRefreshExceptionTwice_throwsAuthenticationException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllRepositories(userId))
            .thenThrow(TryRefreshException.class)
            .thenThrow(TryRefreshException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getAllRepositories(userId));
    }

    @Test
    void getAllBranches_userIdGiven_callsService() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When
        sut.getAllBranches(userId, platformId);

        // Then
        verify(gitLabAccessTokenService).getAllBranches(userId, platformId);
    }

    @Test
    void getAllBranches_serviceReturnsEmptyList_shouldEmptyList() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllBranches(userId, platformId)).thenReturn(List.of());

        // When
        var result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllBranches_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        BranchInternalDTO branch1 = mock(BranchInternalDTO.class);

        when(gitLabAccessTokenService.getAllBranches(userId, platformId)).thenReturn(List.of(branch1));

        // When
        var result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(branch1));
    }

    @Test
    void getAllBranches_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        BranchInternalDTO branch1 = mock(BranchInternalDTO.class);
        BranchInternalDTO branch2 = mock(BranchInternalDTO.class);
        BranchInternalDTO branch3 = mock(BranchInternalDTO.class);

        when(gitLabAccessTokenService.getAllBranches(userId, platformId))
            .thenReturn(List.of(branch1, branch2, branch3));

        // When
        List<BranchInternalDTO> result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(branch1, branch2, branch3));
    }

    @Test
    void getAllBranches_serviceThrowsGitLabException_shouldThrowGitLabException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllBranches(userId, platformId)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllBranches(userId, platformId));
    }

    @Test
    void getAllBranches_serviceThrowsTryRefreshException_shouldCallRefreshService()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllBranches(userId, platformId)).thenThrow(TryRefreshException.class)
                                                                         .thenReturn(emptyList());

        // When
        sut.getAllBranches(userId, platformId);

        // Then
        verify(refreshTokenService).refreshGitAccessToken(userId);
    }

    @Test
    void getAllBranches_serviceThrowsTryRefreshException_shouldCallServiceTwice()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllBranches(userId, platformId)).thenThrow(TryRefreshException.class)
                                                                         .thenReturn(emptyList());

        // When
        sut.getAllBranches(userId, platformId);

        // Then
        verify(gitLabAccessTokenService, times(2)).getAllBranches(userId, platformId);
    }

    @Test
    void getAllBranches_serviceThrowsTryRefreshExceptionTwice_throwsAuthenticationException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getAllBranches(userId, platformId))
            .thenThrow(TryRefreshException.class)
            .thenThrow(TryRefreshException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getAllBranches(userId, platformId));
    }

    @Test
    void getRepositoryById_userIdGiven_callsService() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When
        sut.getRepositoryById(userId, platformId);

        // Then
        verify(gitLabAccessTokenService).getRepositoryById(userId, platformId);
    }

    @Test
    void getRepositoryById_serviceReturnsRepository_shouldReturnRepository() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        NotSavedRepositoryInternalDTO repository = mock(NotSavedRepositoryInternalDTO.class);

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId)).thenReturn(repository);

        // When
        var result = sut.getRepositoryById(userId, platformId);

        // Then
        assertThat(result, equalTo(repository));
    }

    @Test
    void getRepositoryById_serviceThrowsGitLabException_shouldThrowGitLabException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getRepositoryById(userId, platformId));
    }

    @Test
    void getRepositoryById_serviceThrowsTryRefreshException_shouldCallRefreshService()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId)).thenThrow(TryRefreshException.class)
                                                                            .thenReturn(mock(
                                                                                NotSavedRepositoryInternalDTO.class));

        // When
        sut.getRepositoryById(userId, platformId);

        // Then
        verify(refreshTokenService).refreshGitAccessToken(userId);
    }

    @Test
    void getRepositoryById_serviceThrowsTryRefreshException_shouldCallServiceTwice()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(TryRefreshException.class)
            .thenReturn(mock(NotSavedRepositoryInternalDTO.class));

        // When
        sut.getRepositoryById(userId, platformId);

        // Then
        verify(gitLabAccessTokenService, times(2)).getRepositoryById(userId, platformId);
    }

    @Test
    void getRepositoryById_serviceThrowsTryRefreshExceptionTwice_throwsAuthenticationException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(TryRefreshException.class)
            .thenThrow(TryRefreshException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getRepositoryById(userId, platformId));
    }

    @Test
    void getAllCommits_userIdGiven_callsService() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        // When
        sut.getAllCommits(userId, platformId, branch);

        // Then
        verify(gitLabAccessTokenService).getAllCommits(userId, platformId, branch);
    }

    @Test
    void getAllCommits_serviceReturnsEmptyList_shouldEmptyList() throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch)).thenReturn(List.of());

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result, equalTo(emptyList()));
    }

    @Test
    void getAllCommits_serviceReturnsListWithOneElement_shouldEmptyListWithOneElement()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch)).thenReturn(List.of(commit1));

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result.size(), equalTo(1));
        assertThat(result, containsInAnyOrder(commit1));
    }

    @Test
    void getAllCommits_serviceReturnsListWithMultipleElements_shouldReturnListWithMultipleElements()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit3 = mock(CommitInternalDTO.class);

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch))
            .thenReturn(List.of(commit1, commit2, commit3));

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(commit1, commit2, commit3));
    }

    @Test
    void getAllCommits_serviceThrowsGitLabException_shouldThrowGitLabException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllCommits(userId, platformId, branch));
    }

    @Test
    void getAllCommits_serviceThrowsTryRefreshException_shouldCallRefreshService()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch)).thenThrow(TryRefreshException.class)
                                                                                .thenReturn(emptyList());

        // When
        sut.getAllCommits(userId, platformId, branch);

        // Then
        verify(refreshTokenService).refreshGitAccessToken(userId);
    }

    @Test
    void getAllCommits_serviceThrowsTryRefreshException_shouldCallServiceTwice()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch))
            .thenThrow(TryRefreshException.class)
            .thenReturn(emptyList());

        // When
        sut.getAllCommits(userId, platformId, branch);

        // Then
        verify(gitLabAccessTokenService, times(2)).getAllCommits(userId, platformId, branch);
    }

    @Test
    void getAllCommits_serviceThrowsTryRefreshExceptionTwice_throwsAuthenticationException()
        throws GitException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();

        when(gitLabAccessTokenService.getAllCommits(userId, platformId, branch))
            .thenThrow(TryRefreshException.class)
            .thenThrow(TryRefreshException.class);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getAllCommits(userId, platformId, branch));
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
    void repositoryAccessibleByUser_serviceThrowsTryRefreshExceptionAndReturnsObject_shouldReturnTrue()
        throws GitLabException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(TryRefreshException.class)
            .thenReturn(mock(NotSavedRepositoryInternalDTO.class));

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(true));
    }

    @Test
    void repositoryAccessibleByUser_serviceThrowsTryRefreshExceptionTwoTimes_shouldReturnFalse()
        throws GitLabException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(TryRefreshException.class)
            .thenThrow(TryRefreshException.class);

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(false));
    }

    @Test
    void repositoryAccessibleByUser_serviceThrowsGitLabException_shouldReturnFalse()
        throws GitLabException, TryRefreshException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        when(gitLabAccessTokenService.getRepositoryById(userId, platformId))
            .thenThrow(GitLabException.class);

        // When
        boolean result = sut.repositoryAccessibleByUser(userId, platformId);

        // Then
        assertThat(result, equalTo(false));
    }
}