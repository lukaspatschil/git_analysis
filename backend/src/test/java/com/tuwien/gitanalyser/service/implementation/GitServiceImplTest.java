package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import com.tuwien.gitanalyser.service.apiCalls.GitHubAPI;
import com.tuwien.gitanalyser.service.apiCalls.GitLabAPI;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitServiceImplTest {

    private final String defaultBranch = "develop";
    GitServiceImpl sut;
    private UserService userService;
    private RepositoryService repositoryService;
    private GitHubAPI gitHubAPI;
    private GitLabAPI gitLabAPI;
    private String exceptionString;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        repositoryService = mock(RepositoryService.class);
        gitHubAPI = mock(GitHubAPI.class);
        gitLabAPI = mock(GitLabAPI.class);
        sut = new GitServiceImpl(userService, repositoryService, gitHubAPI, gitLabAPI);
        exceptionString = "testException";
    }

    @Test
    void getAllRepositories_gitLabAuthorization_shouldCallGitLabAPI()
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
    void getAllRepositories_gitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndNoRepositoryExists_returnsEmptyList()
        throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getAllRepositories(accessToken)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndOneRepositoryExists_returnsRepository()
        throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getAllRepositories(accessToken)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndMultipleRepositoriesExist_returnsRepositories()
        throws IOException, NotFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getAllRepositories(accessToken)).thenReturn(List.of(repository1, repository2));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1, repository2));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndNoRepositoryExists_returnsEmptyList()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndOneRepositoryExists_returnsRepository()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndMultipleRepositoriesExist_returnsRepositories()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenReturn(List.of(repository1, repository2));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1, repository2));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndMultipleRepositoriesExist_callsRepositoryServiceCleanUp()
        throws IOException, NotFoundException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 =
            NotSavedRepositoryInternalDTO.builder().platformId(Randoms.getLong()).build();
        NotSavedRepositoryInternalDTO repository2 =
            NotSavedRepositoryInternalDTO.builder().platformId(Randoms.getLong()).build();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenReturn(List.of(repository1, repository2));

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(repositoryService).deleteAllNotAccessibleRepositoryEntities(userId, List.of(repository1.getPlatformId(), repository2.getPlatformId()));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationThrowsException_shouldThrowException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long userId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getAllRepositories(accessToken)).thenThrow(new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_gitHubAuthorizationThrowsException_shouldThrowException()
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
    void getRepositoryById_gitLabAuthorization_shouldCallGitLabAPI()
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
    void getRepositoryById_gitHubAuthorization_shouldCallGitHubAPI()
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
    void getAllBranches_gitLabAuthorization_shouldCallGitLabAPI()
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
    void getAllBranches_gitHubAuthorization_shouldCallGitHubAPI()
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
    void getAllBranches_gitLabAuthorizationThrowsException_shouldThrowRuntimeException()
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
    void getAllBranches_gitHubAuthorizationThrowsException_shouldThrowException()
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
    void getAllCommits_gitHubAuthorizationRepoExists_shouldCallGitHubApi()
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
    void getAllCommits_gitHubAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExists_shouldCallGitLabApi()
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
    void getAllCommits_gitLabAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }

    @Test
    void getAllCommitters_gitLabAuthorizationRepoExists_shouldCallGitLabApi()
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
    void getAllCommitters_gitLabAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllCommitters_gitLabAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneItem()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mockCommitInternalDTO();
        CommitterInternalDTO committerInternalDTO = new CommitterInternalDTO(commit.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken, commit);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(committerInternalDTO));
    }

    @Test
    void getAllCommitters_gitLabAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoItems()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mockCommitInternalDTO();
        CommitInternalDTO commit2 = mockCommitInternalDTO();
        CommitterInternalDTO committerInternalDTO1 = new CommitterInternalDTO(commit1.getAuthor());
        CommitterInternalDTO committerInternalDTO2 = new CommitterInternalDTO(commit2.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken, commit1, commit2);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(committerInternalDTO1, committerInternalDTO2));
    }

    @Test
    void getAllCommitters_gitLabAuthorizationRepoExistsAndTwoCommitsWithSameCommitter_shouldReturnListWithOneItem()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        String name = Randoms.alpha();
        CommitInternalDTO commit1 = mockCommitInternalDTO(name);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name);
        CommitterInternalDTO committerInternalDTO1 = new CommitterInternalDTO(commit1.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabAPI, repositoryId, accessToken, commit1, commit2);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, equalTo(Set.of(committerInternalDTO1)));
    }

    @Test
    void getAllCommitters_gitHubAuthorizationRepoExists_shouldCallGitLabApi()
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
    void getAllCommitters_gitHubAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllCommitters_gitHubAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneItem()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mockCommitInternalDTO();
        CommitterInternalDTO committerInternalDTO = new CommitterInternalDTO(commit.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken, commit);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(committerInternalDTO));
    }

    @Test
    void getAllCommitters_gitHubAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoItems()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mockCommitInternalDTO();
        CommitInternalDTO commit2 = mockCommitInternalDTO();
        CommitterInternalDTO committerInternalDTO1 = new CommitterInternalDTO(commit1.getAuthor());
        CommitterInternalDTO committerInternalDTO2 = new CommitterInternalDTO(commit2.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken, commit1, commit2);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(committerInternalDTO1, committerInternalDTO2));
    }

    @Test
    void getAllCommitters_gitHubAuthorizationRepoExistsAndTwoCommitsWithSameCommitter_shouldReturnListWithOneItem()
        throws NotFoundException, IOException, GitLabApiException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        String name = Randoms.alpha();
        CommitInternalDTO commit1 = mockCommitInternalDTO(name);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name);
        CommitterInternalDTO committerInternalDTO1 = new CommitterInternalDTO(commit1.getAuthor());

        String accessToken = prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubAPI, repositoryId, accessToken, commit1, commit2);

        // When
        Set<CommitterInternalDTO> result = sut.getAllCommitters(userId, repositoryId, defaultBranch);

        // Then
        assertThat(result, equalTo(Set.of(committerInternalDTO1)));
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

    private void mockGitApiGetAllCommits(GitAPI gitHubAPI,
                                         long repositoryId,
                                         String accessToken,
                                         CommitInternalDTO... commits)
        throws IOException, GitLabApiException {
        when(gitHubAPI.getAllCommits(accessToken, repositoryId, defaultBranch))
            .thenReturn(Arrays.stream(commits).toList());
    }

    private CommitInternalDTO mockCommitInternalDTO(String name) {
        CommitInternalDTO commit = mock(CommitInternalDTO.class);
        when(commit.getAuthor()).thenReturn(name);
        return commit;
    }

    private CommitInternalDTO mockCommitInternalDTO() {
        CommitInternalDTO commit = mock(CommitInternalDTO.class);
        when(commit.getAuthor()).thenReturn(Randoms.alpha());
        return commit;
    }
}