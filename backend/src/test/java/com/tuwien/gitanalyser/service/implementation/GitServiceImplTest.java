package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitHubException;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import com.tuwien.gitanalyser.service.apiCalls.github.GitHubExceptionHandlerServiceImpl;
import com.tuwien.gitanalyser.service.apiCalls.gitlab.GitLabExceptionHandlerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitServiceImplTest {

    GitServiceImpl sut;
    private UserService userService;
    private RepositoryService repositoryService;
    private String exceptionString;
    private String defaultBranch;
    private GitHubExceptionHandlerServiceImpl gitHubService;
    private GitLabExceptionHandlerServiceImpl gitLabService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        repositoryService = mock(RepositoryService.class);
        gitHubService = mock(GitHubExceptionHandlerServiceImpl.class);
        gitLabService = mock(GitLabExceptionHandlerServiceImpl.class);

        sut = new GitServiceImpl(userService, repositoryService,
                                 gitHubService,
                                 gitLabService);
        exceptionString = "testException";
        defaultBranch = Randoms.alpha();
    }

    @Test
    void getAllRepositories_gitLabAuthorization_shouldCallGitLabService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_gitHubAuthorization_shouldCallGitHubService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        // When
        prepareUserService(userId, AuthenticationProvider.GITHUB);
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndNoRepositoryExists_returnsEmptyList()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndOneRepositoryExists_returnsRepository()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubService.getAllRepositories(userId)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_gitHubAuthorizationAndMultipleRepositoriesExist_returnsRepositories()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubService.getAllRepositories(userId)).thenReturn(List.of(repository1, repository2));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1, repository2));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndNoRepositoryExists_returnsEmptyList()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, empty());
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndOneRepositoryExists_returnsRepository()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllRepositories(userId)).thenReturn(List.of(repository1));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndMultipleRepositoriesExist_returnsRepositories()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 = mock(NotSavedRepositoryInternalDTO.class);
        NotSavedRepositoryInternalDTO repository2 = mock(NotSavedRepositoryInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllRepositories(userId)).thenReturn(List.of(repository1, repository2));

        // When
        List<NotSavedRepositoryInternalDTO> result = sut.getAllRepositories(userId);

        // Then
        assertThat(result, containsInAnyOrder(repository1, repository2));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationAndMultipleRepositoriesExist_callsRepositoryServiceCleanUp()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        NotSavedRepositoryInternalDTO repository1 =
            NotSavedRepositoryInternalDTO.builder().platformId(Randoms.getLong()).build();
        NotSavedRepositoryInternalDTO repository2 =
            NotSavedRepositoryInternalDTO.builder().platformId(Randoms.getLong()).build();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllRepositories(userId)).thenReturn(List.of(repository1, repository2));

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(repositoryService).deleteAllNotAccessibleRepositoryEntities(userId,
                                                                           List.of(repository1.getPlatformId(),
                                                                                   repository2.getPlatformId()));
    }

    @Test
    void getAllRepositories_gitLabAuthorizationThrowsGitLabException_shouldThrowGitException()
        throws NotFoundException, GitException {
        // Given
        long userId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllRepositories(userId)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_gitHubAuthorizationThrowsGitHubException_shouldThrowGitException()
        throws NotFoundException, GitException {
        // Given
        long userId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubService.getAllRepositories(userId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_randomAuthorization_shouldThrowException() {
        // Given

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(Randoms.getLong()));
    }

    @Test
    void getRepositoryById_gitLabAuthorization_shouldCallGitLabService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();
        var notSavedRepositoryInternalDTO = getRandomNotSavedRepositoryInternalDTO(repositoryId);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetRepositoryById(gitLabService, userId, repositoryId, notSavedRepositoryInternalDTO);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitLabService).getRepositoryById(userId, repositoryId);
    }

    @Test
    void getRepositoryById_gitHubAuthorization_shouldCallGitHubService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();
        var notSavedRepositoryInternalDTO = getRandomNotSavedRepositoryInternalDTO(repositoryId);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetRepositoryById(gitHubService, userId, repositoryId, notSavedRepositoryInternalDTO);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitHubService).getRepositoryById(userId, repositoryId);
    }

    @Test
    void getRepositoryById_randomAuthorization_shouldThrowException() throws NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();
        Long userId = Randoms.getLong();

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getRepositoryById(userId, repositoryId));
    }

    @Test
    void getAllBranches_gitLabAuthorization_shouldCallGitLabService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitLabService).getAllBranches(userId, repositoryId);
    }

    @Test
    void getAllBranches_gitHubAuthorization_shouldCallGitHubService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitHubService).getAllBranches(userId, repositoryId);
    }

    @Test
    void getAllBranches_gitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws NotFoundException, GitException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        when(gitLabService.getAllBranches(userId, repositoryId)).thenThrow(GitLabException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllBranches_gitHubAuthorizationThrowsException_shouldThrowException()
        throws NotFoundException, GitException {
        // Given
        long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        when(gitHubService.getAllBranches(userId, repositoryId)).thenThrow(GitHubException.class);

        // When + Then
        assertThrows(GitHubException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExists_shouldCallGitHubService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        verify(gitHubService).getAllCommits(userId, repositoryId, defaultBranch);
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, repositoryId, userId, defaultBranch);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, repositoryId, userId, defaultBranch, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_gitHubAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, repositoryId, userId, defaultBranch, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExists_shouldCallGitLabService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        // When
        sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        verify(gitLabService).getAllCommits(userId, repositoryId, defaultBranch);
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExistsAndNoCommits_shouldReturnEmptyList()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, repositoryId, userId, defaultBranch);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, empty());
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExistsAndOneCommits_shouldReturnListWithOneCommit()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit = mock(CommitInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, repositoryId, userId, defaultBranch, commit);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit)));
    }

    @Test
    void getAllCommits_gitLabAuthorizationRepoExistsAndTwoCommits_shouldReturnListWithTwoCommits()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long repositoryId = Randoms.getLong();
        CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
        CommitInternalDTO commit2 = mock(CommitInternalDTO.class);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, repositoryId, userId, defaultBranch, commit1, commit2);

        // When
        List<CommitInternalDTO> allCommits = sut.getAllCommits(userId, repositoryId, defaultBranch);

        // Then
        assertThat(allCommits, equalTo(List.of(commit1, commit2)));
    }

    @Test
    void getStats_gitHubAuthorizationAndOneCommit_shouldReturnOneStatsObject() throws GitException,
                                                                                      NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name = Randoms.alpha();
        CommitInternalDTO commit = mockCommitInternalDTO(name);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, platformId, userId, defaultBranch, commit);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder(allOf(
            hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name)),
            hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits, equalTo(1)),
            hasFeature("numberOfAdditions", StatsInternalDTO::getNumberOfAdditions, equalTo(commit.getAdditions())),
            hasFeature("numberOfDeletions", StatsInternalDTO::getNumberOfDeletions, equalTo(commit.getDeletions()))
        )));
    }

    @Test
    void getStats_gitHubAuthorizationAndTwoCommitsWithSameName_shouldReturnOneStatsObject()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name = Randoms.alpha();

        CommitInternalDTO commit1 = mockCommitInternalDTO(name);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, platformId, userId, defaultBranch, commit1, commit2);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder(allOf(
            hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name)),
            hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits, equalTo(2)),
            hasFeature("numberOfAdditions", StatsInternalDTO::getNumberOfAdditions,
                       equalTo(commit1.getAdditions() + commit2.getAdditions())),
            hasFeature("numberOfDeletions",
                       StatsInternalDTO::getNumberOfDeletions,
                       equalTo(commit1.getDeletions() + commit2.getDeletions()))
        )));
    }

    @Test
    void getStats_gitHubAuthorizationAndTwoCommitsWithDifferentName_shouldReturnTwoStatsObject()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name1 = Randoms.alpha();
        String name2 = Randoms.alpha();

        CommitInternalDTO commit1 = mockCommitInternalDTO(name1);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name2);

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, platformId, userId, defaultBranch, commit1, commit2);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder(
            allOf(
                hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name1)),
                hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits,
                           equalTo(1)),
                hasFeature("numberOfAdditions",
                           StatsInternalDTO::getNumberOfAdditions,
                           equalTo(commit1.getAdditions())),
                hasFeature("numberOfDeletions",
                           StatsInternalDTO::getNumberOfDeletions,
                           equalTo(commit1.getDeletions()))
            ),
            allOf(
                hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name2)),
                hasFeature("numberOfCommits",
                           StatsInternalDTO::getNumberOfCommits,
                           equalTo(1)),
                hasFeature("numberOfAdditions",
                           StatsInternalDTO::getNumberOfAdditions,
                           equalTo(commit2.getAdditions())),
                hasFeature("numberOfDeletions",
                           StatsInternalDTO::getNumberOfDeletions,
                           equalTo(commit2.getDeletions()))
            ))
        );
    }

    @Test
    void getStats_gitHubAuthorizationAndNoCommitsAvailable_shouldReturnEmptyList()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITHUB);
        mockGitApiGetAllCommits(gitHubService, platformId, userId, defaultBranch);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(0));
    }

    @Test
    void getStats_gitLabAuthorizationAndOneCommit_shouldReturnOneStatsObject()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name = Randoms.alpha();
        CommitInternalDTO commit = mockCommitInternalDTO(name);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, platformId, userId, defaultBranch, commit);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder(allOf(
            hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name)),
            hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits, equalTo(1)),
            hasFeature("numberOfAdditions", StatsInternalDTO::getNumberOfAdditions, equalTo(commit.getAdditions())),
            hasFeature("numberOfDeletions", StatsInternalDTO::getNumberOfDeletions, equalTo(commit.getDeletions()))
        )));
    }

    @Test
    void getStats_gitLabAuthorizationAndTwoCommitsWithSameName_shouldReturnOneStatsObject()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name = Randoms.alpha();

        CommitInternalDTO commit1 = mockCommitInternalDTO(name);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, platformId, userId, defaultBranch, commit1, commit2);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder(allOf(
            hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name)),
            hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits, equalTo(2)),
            hasFeature("numberOfAdditions", StatsInternalDTO::getNumberOfAdditions,
                       equalTo(commit1.getAdditions() + commit2.getAdditions())),
            hasFeature("numberOfDeletions",
                       StatsInternalDTO::getNumberOfDeletions,
                       equalTo(commit1.getDeletions() + commit2.getDeletions()))
        )));
    }

    @Test
    void getStats_gitLabAuthorizationAndTwoCommitsWithDifferentName_shouldReturnTwoStatsObject()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        String name1 = Randoms.alpha();
        String name2 = Randoms.alpha();

        CommitInternalDTO commit1 = mockCommitInternalDTO(name1);
        CommitInternalDTO commit2 = mockCommitInternalDTO(name2);

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, platformId, userId, defaultBranch, commit1, commit2);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder(
            allOf(
                hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name1)),
                hasFeature("numberOfCommits", StatsInternalDTO::getNumberOfCommits,
                           equalTo(1)),
                hasFeature("numberOfAdditions",
                           StatsInternalDTO::getNumberOfAdditions,
                           equalTo(commit1.getAdditions())),
                hasFeature("numberOfDeletions",
                           StatsInternalDTO::getNumberOfDeletions,
                           equalTo(commit1.getDeletions()))
            ),
            allOf(
                hasFeature("name", StatsInternalDTO::getCommitter, equalTo(name2)),
                hasFeature("numberOfCommits",
                           StatsInternalDTO::getNumberOfCommits,
                           equalTo(1)),
                hasFeature("numberOfAdditions",
                           StatsInternalDTO::getNumberOfAdditions,
                           equalTo(commit2.getAdditions())),
                hasFeature("numberOfDeletions",
                           StatsInternalDTO::getNumberOfDeletions,
                           equalTo(commit2.getDeletions()))
            ))
        );
    }

    @Test
    void getStats_gitLabAuthorizationAndNoCommitsAvailable_shouldReturnEmptyList()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        prepareUserService(userId, AuthenticationProvider.GITLAB);
        mockGitApiGetAllCommits(gitLabService, platformId, userId, defaultBranch);

        // When
        List<StatsInternalDTO> result = sut.getStats(userId, platformId, defaultBranch);

        // Then
        assertThat(result, hasSize(0));
    }

    @Test
    void getEmail_gitLabAuthorization_shouldCallGitLabService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        prepareUserService(userId, AuthenticationProvider.GITLAB);

        // When
        sut.getEmail(userId);

        // Then
        verify(gitLabService).getEmail(userId);
    }

    @Test
    void getEmail_gitHubAuthorization_shouldCallGitHubService()
        throws NotFoundException, GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        prepareUserService(userId, AuthenticationProvider.GITHUB);

        // When
        sut.getEmail(userId);

        // Then
        verify(gitHubService).getEmail(userId);
    }

    @Test
    void getEmail_randomAuthorization_shouldThrowException() throws NotFoundException {
        // Given
        long userId = Randoms.getLong();

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getEmail(userId));
    }

    private void prepareUserService(long userId, AuthenticationProvider authenticationProvider)
        throws NotFoundException {
        User user = mock(User.class);
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAuthenticationProvider()).thenReturn(authenticationProvider);
    }

    private NotSavedRepositoryInternalDTO getRandomNotSavedRepositoryInternalDTO(long repositoryId) {
        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(repositoryId)
                                            .name(Randoms.alpha())
                                            .url(Randoms.alpha())
                                            .build();
    }

    private void mockGitApiGetRepositoryById(GitExceptionHandlerService gitServiceProvider,
                                             long userId,
                                             long repositoryId,
                                             NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO)
        throws GitException {
        when(gitServiceProvider.getRepositoryById(userId, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
    }

    private void mockGitApiGetAllCommits(GitExceptionHandlerService gitServiceProvider,
                                         long repositoryId,
                                         long userId,
                                         String branch,
                                         CommitInternalDTO... commits)
        throws GitException {
        when(gitServiceProvider.getAllCommits(userId, repositoryId, branch))
            .thenReturn(Arrays.stream(commits).toList());
    }

    private CommitInternalDTO mockCommitInternalDTO(String name) {
        CommitInternalDTO commit = mock(CommitInternalDTO.class);
        when(commit.getAuthor()).thenReturn(name);
        when(commit.getAdditions()).thenReturn(Randoms.integer(0, 10));
        when(commit.getDeletions()).thenReturn(Randoms.integer(0, 10));
        return commit;
    }
}