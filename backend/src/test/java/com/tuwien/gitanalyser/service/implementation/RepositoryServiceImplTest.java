package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.UserService;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryServiceImplTest {

    private final String defaultBranch = "develop";
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
        sut = new RepositoryServiceImpl(userService,
                                        gitHubAPI,
                                        gitLabAPI
        );
        exceptionString = "testException";
    }

    @Test
    void getAllRepositories_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenThrow(new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_GitHubAuthorizationThrowsException_shouldThrowException()
        throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getAllRepositories(accessToken)).thenThrow(new IOException(exceptionString));

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
    void getRepositoryById_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();
        var notSavedRepositoryInternalDTO = getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetRepositoryById(gitLabAPI, accessToken, repositoryId, notSavedRepositoryInternalDTO);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitLabAPI).getRepositoryById(accessToken, repositoryId);
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldCallGitHubAPI()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();
        var notSavedRepositoryInternalDTO = getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetRepositoryById(gitHubAPI, accessToken, repositoryId, notSavedRepositoryInternalDTO);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitHubAPI).getRepositoryById(accessToken, repositoryId);
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
    void getAllBranches_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitLabAPI).getAllBranches(accessToken, repositoryId);
    }

    @Test
    void getAllBranches_GitHubAuthorization_shouldCallGitHubAPI()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitHubAPI).getAllBranches(accessToken, repositoryId);
    }

    @Test
    void getAllBranches_GitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllBranches(accessToken, repositoryId)).thenThrow(new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(GitLabApiException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllBranches_GitHubAuthorizationThrowsException_shouldThrowException()
        throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getAllBranches(accessToken, repositoryId)).thenThrow(new IOException(exceptionString));

        // When + Then
        assertThrows(IOException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllBranches_randomAuthorization_shouldThrowException() {
        // Given

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllBranches(Randoms.getLong(), Randoms.getLong()));
    }

    @Test
    void getAllCommits_GitHubAuthorizationRepoExists_shouldCallGitHubApi()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        verify(gitHubAPI).getAllCommits(accessToken, repositoryId, defaultBranch);
    }

    @Test
    void getAllCommits_GitHubAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitHubGetAllCommits(gitHubAPI, repositoryId, accessToken);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_GitHubAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitHubGetAllCommits(gitHubAPI, repositoryId, accessToken, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_GitHubAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitHubGetAllCommits(gitHubAPI, repositoryId, accessToken, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }

    @Test
    void getAllCommits_GitLabAuthorizationRepoExists_shouldCallGitLabApi()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);

        // When
        sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        verify(gitLabAPI).getAllCommits(accessToken, repositoryId, defaultBranch);
    }

    @Test
    void getAllCommits_GitLabAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitHubGetAllCommits(gitLabAPI, repositoryId, accessToken);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_GitLabAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitHubGetAllCommits(gitLabAPI, repositoryId, accessToken, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_GitLabAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitHubGetAllCommits(gitLabAPI, repositoryId, accessToken, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }



    private void mockGitHubGetAllCommits(GitAPI gitHubAPI, long repositoryId, String accessToken,
                                         CommitInternalDTO... commits)
        throws IOException, GitLabApiException {
        when(gitHubAPI.getAllCommits(accessToken, repositoryId, defaultBranch))
            .thenReturn(Arrays.stream(commits).toList());
    }

    private String prepareUserService(long userId, AuthenticationProvider authenticationProvider)
        throws NotFoundException {
        User user = mock(User.class);
        String accessToken = Randoms.alpha();
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAccessToken()).thenReturn(accessToken);
        when(user.getAuthenticationProvider()).thenReturn(authenticationProvider);
        return accessToken;
    }

    private NotSavedRepositoryInternalDTO getRandomNotSavedRepositoryInternalDTO(long repositoryId) {
        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(repositoryId)
                                            .name(Randoms.alpha())
                                            .url(Randoms.alpha())
                                            .build();
    }

    private void mockGitApiGetRepositoryById(GitAPI gitApi,
                                             String accessToken,
                                             long repositoryId,
                                             NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO)
        throws GitLabApiException, IOException {
        when(gitApi.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
    }
}
