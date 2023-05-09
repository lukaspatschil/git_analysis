package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitHubException;
import com.tuwien.gitanalyser.service.UserService;
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

class GitHubAccessTokenServiceImplTest {

    private GitHubAPI gitHubAPI;
    private UserService userService;
    private GitHubAccessTokenServiceImpl sut;

    @BeforeEach
    void setUp() {
        gitHubAPI = mock(GitHubAPI.class);
        userService = mock(UserService.class);
        sut = new GitHubAccessTokenServiceImpl(gitHubAPI, userService);
    }

    @Test
    void getAllRepositories_userAndAccessTokenExist_shouldCallService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        prepareGetAllRepositories(accessToken, List.of());

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

        String accessToken = prepareUser(userId);
        prepareGetAllRepositories(accessToken, List.of(repository1));

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

        String accessToken = prepareUser(userId);
        prepareGetAllRepositories(accessToken, List.of(repository1, repository2, repository3));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result.size(), equalTo(3));
        assertThat(result, containsInAnyOrder(repository1, repository2, repository3));
    }

    @Test
    void getAllRepositories_serviceThrowsException_shouldThrowException() throws GitException {
        // Given
        long userId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        when(gitHubAPI.getAllRepositories(accessToken)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllRepositories(userId));

    }

    @Test
    void getAllBranches_userAndAccessTokenExist_shouldCallService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        // When
        sut.getAllBranches(userId, platformId);

        // Then
        verify(gitHubAPI).getAllBranches(accessToken, platformId);
    }

    @Test
    void getAllBranches_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        prepareGetAllBranches(platformId, accessToken, List.of());

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

        String accessToken = prepareUser(userId);
        prepareGetAllBranches(platformId, accessToken, List.of(branch1));

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

        String accessToken = prepareUser(userId);
        prepareGetAllBranches(platformId, accessToken, List.of(branch1, branch2, branch3));

        // When
        var result = sut.getAllBranches(userId, platformId);

        // Then
        assertThat(result.size(), equalTo(3));
        MatcherAssert.assertThat(result, containsInAnyOrder(branch1, branch2, branch3));
    }

    @Test
    void getAllBranches_serviceThrowsException_shouldThrowException() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        when(gitHubAPI.getAllBranches(accessToken, platformId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllBranches(userId, platformId));
    }

    @Test
    void getRepositoryById_userAndAccessTokenExist_shouldCallService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        // When
        sut.getRepositoryById(userId, platformId);

        // Then
        verify(gitHubAPI).getRepositoryById(accessToken, platformId);
    }

    @Test
    void getRepositoryById_serviceReturnsRepository_returnsRepository() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        NotSavedRepositoryInternalDTO repository = mock(NotSavedRepositoryInternalDTO.class);
        when(gitHubAPI.getRepositoryById(accessToken, platformId)).thenReturn(repository);

        // When
        var result = sut.getRepositoryById(userId, platformId);

        // Then
        assertThat(result, equalTo(repository));
    }

    @Test
    void getRepositoryById_serviceThrowsException_shouldThrowException() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String accessToken = prepareUser(userId);

        when(gitHubAPI.getRepositoryById(accessToken, platformId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getRepositoryById(userId, platformId));
    }

    @Test
    void getAllCommits_userAndAccessTokenExist_shouldCallService() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String accessToken = prepareUser(userId);

        // When
        sut.getAllCommits(userId, platformId, branch);

        // Then
        verify(gitHubAPI).getAllCommits(accessToken, platformId, branch);
    }

    @Test
    void getAllCommits_serviceReturnsEmptyList_shouldEmptyList() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String accessToken = prepareUser(userId);

        prepareGetAllCommits(platformId, branch, accessToken, List.of());

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

        String accessToken = prepareUser(userId);
        prepareGetAllCommits(platformId, branch, accessToken, List.of(commit1));

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

        String accessToken = prepareUser(userId);
        prepareGetAllCommits(platformId, branch, accessToken, List.of(commit1, commit2, commit3));

        // When
        var result = sut.getAllCommits(userId, platformId, branch);

        // Then
        assertThat(result.size(), equalTo(3));
        MatcherAssert.assertThat(result, containsInAnyOrder(commit1, commit2, commit3));
    }

    @Test
    void getAllCommits_serviceThrowsException_shouldThrowException() throws GitException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String accessToken = prepareUser(userId);

        when(gitHubAPI.getAllCommits(accessToken, platformId, branch)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllCommits(userId, platformId, branch));
    }

    private String prepareUser(long userId) {
        String accessToken = Randoms.alpha();
        User user = mock(User.class);
        when(user.getAccessToken()).thenReturn(accessToken);

        when(userService.getUser(userId)).thenReturn(user);
        return accessToken;
    }

    private void prepareGetAllBranches(long platformId, String accessToken, List<BranchInternalDTO> listOfBranches)
        throws GitHubException {
        when(gitHubAPI.getAllBranches(accessToken, platformId)).thenReturn(listOfBranches);
    }

    private void prepareGetAllRepositories(String accessToken, List<NotSavedRepositoryInternalDTO> listOfRepositories)
        throws GitHubException {
        when(gitHubAPI.getAllRepositories(accessToken)).thenReturn(listOfRepositories);
    }

    private void prepareGetAllCommits(long platformId, String branch, String accessToken,
                                      List<CommitInternalDTO> listOfCommits) throws GitHubException {
        when(gitHubAPI.getAllCommits(accessToken, platformId, branch)).thenReturn(listOfCommits);
    }
}