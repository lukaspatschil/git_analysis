package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RepositoryDTO;
import com.tuwien.gitanalyser.entity.Repository;
import io.restassured.response.Response;
import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.models.Project;
import org.junit.Test;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.http.HttpStatus;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.Matchers.branchDTOMatcher;
import static utils.Matchers.committerDTOMatcher;
import static utils.Matchers.repositoryMatcher;

public class GitIntegrationTest extends BaseIntegrationTest {

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";

    private static final String BRANCHES_ENDPOINT_EXTENSION = "branch";

    private static final String COMMITTER_ENDPOINT_EXTENSION = "committer";

    // TODO: positive test case for github

    @Test
    public void queryAllRepositories_userGitHubUserExists_shouldSend200() throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = callGetRestEndpoint(gitHubUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllRepositories_userGitHubUserExistsAndNoRepositoriesExist_shouldReturnEmptyList()
        throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = callGetRestEndpoint(gitHubUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.as(RepositoryDTO[].class).length, is(0));
    }

    @Test
    public void queryAllRepositories_userLabUserExists_shouldSend200() throws GitLabApiException {
        // Given
        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of());
        gitLabMockMemberProjects(projectApi, List.of());

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndNoRepositoriesExist_shouldReturnEmptyList()
        throws GitLabApiException {
        // Given
        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of());
        gitLabMockMemberProjects(projectApi, List.of());

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.as(NotSavedRepositoryDTO[].class).length, is(0));
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndOneMemberAndOneOwnedRepositoryExist_shouldReturn2Elements()
        throws GitLabApiException {
        // Given
        Project memberedProject = gitLabCreateRandomProject();
        Project ownedProject = gitLabCreateRandomProject();

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of(memberedProject));
        gitLabMockMemberProjects(projectApi, List.of(ownedProject));

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.as(NotSavedRepositoryDTO[].class).length, is(2));
        NotSavedRepositoryDTO[] repositories = response.as(NotSavedRepositoryDTO[].class);

        assertThat(Arrays.asList(repositories), containsInAnyOrder(
            repositoryMatcher(ownedProject),
            repositoryMatcher(memberedProject)
        ));
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndOneMemberProject_shouldDeleteOtherRepositories()
        throws GitLabApiException {
        // Given
        Project memberedProject = gitLabCreateRandomProject();

        Repository repository = addRepository(gitLabUser, Randoms.getLong());

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of(memberedProject));

        // When
        callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        Optional<Repository> resultRepository = repositoryRepository.findById(repository.getId());
        assertThat(resultRepository, isEmpty());
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndOneMemberProjectWithEqualPlatformIds_shouldNotDeleteOtherRepositories()
        throws GitLabApiException {
        // Given
        Project memberedProject = gitLabCreateRandomProject();

        Repository repository = addRepository(gitLabUser, memberedProject.getId());

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of(memberedProject));

        // When
        callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        Optional<Repository> resultRepository = repositoryRepository.findById(repository.getId());
        assertThat(resultRepository, isPresent());
    }

    @Test
    public void queryRepositoriesById_userGitLabUserExistsProjectWithIdExists_shouldReturnThisRepo()
        throws GitLabApiException {
        // Given
        Project project = gitLabCreateRandomProject();

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockGetProject(projectApi, project);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT + "/" + project.getId());

        // Then
        RepositoryDTO repositories = response.as(RepositoryDTO.class);

        assertThat(repositories, equalTo(
            new RepositoryDTO(project.getId(),
                              project.getName(),
                              project.getHttpUrlToRepo())
        ));
    }


    // TODO: positive test case for github
    @Test
    public void queryAllBranches_userGitHubUserExists_shouldSend200() throws IOException {
        // Given
        Long repositoryId = Randoms.getLong();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);
        gitHubMockBranches(ghRepository);
        // When
        Response response = callGetRestEndpoint(
            gitHubUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllBranches_userGitHubUserExistsAndNoBranchesExist_shouldReturnEmptyList() throws IOException {
        // Given
        Long repositoryId = Randoms.getLong();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);
        gitHubMockBranches(ghRepository);

        // When
        Response response = callGetRestEndpoint(
            gitHubUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.as(BranchDTO[].class).length, is(0));
    }

    @Test
    public void queryAllBranches_userLabUserExists_shouldSend200() throws GitLabApiException {
        // Given
        Long repositoryId = Randoms.getLong();
        GitLabApi gitLabApi = gitLabMockFactory();
        RepositoryApi repositoryApi = gitLabMockRepositoryApi(gitLabApi);
        gitLabMockGetBranches(repositoryId, repositoryApi, List.of());

        // When
        Response response = callGetRestEndpoint(
            gitLabUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllBranches_userGitLabUserExistsAndRepositoriesExistAndNoBranches_shouldReturnEmptyList()
        throws GitLabApiException {
        // Given
        Project project = gitLabCreateRandomProject();
        GitLabApi gitLabApi = gitLabMockFactory();
        RepositoryApi repositoryApi = gitLabMockRepositoryApi(gitLabApi);
        gitLabMockGetBranches(project.getId(), repositoryApi, List.of());

        // When
        Response response = callGetRestEndpoint(
            gitLabUserToken, REPOSITORY_ENDPOINT + "/" + project.getId() + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.as(BranchDTO[].class).length, is(0));
    }

    @Test
    public void queryAllBranches_userGitLabUserExistsAndRepositoryExistAnd2Branches_shouldReturn2Branches()
        throws GitLabApiException {
        // Given
        Long repositoryId = 1L;

        Branch branch1 = new Branch().withName("master");
        Branch branch2 = new Branch().withName("develop");

        GitLabApi gitLabApi = gitLabMockFactory();
        RepositoryApi repositoryApi = gitLabMockRepositoryApi(gitLabApi);
        gitLabMockGetBranches(repositoryId, repositoryApi, List.of(branch1, branch2));

        // When
        Response response = callGetRestEndpoint(
            gitLabUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.as(BranchDTO[].class).length, equalTo(2));
        BranchDTO[] branchDTOS = response.as(BranchDTO[].class);

        assertThat(Arrays.asList(branchDTOS), containsInAnyOrder(
            branchDTOMatcher(branch1),
            branchDTOMatcher(branch2)
        ));
    }

    // TODO positive tests for get commits from github

    @Test
    public void queryAllCommitter_gitLabUser_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockCommit();
        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION
                                + "?branch=" + branch);

        // Then
        verify(gitLabApi).getCommitsApi();
    }
    @Test
    public void queryAllCommitter_gitLabUserAndOneCommitAvailable_shouldReturnCommitter() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockCommit();
        CommitterDTO committerDTO = mockCommitterDTO(commit);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), containsInAnyOrder(equalTo(committerDTO)));
    }

    @Test
    public void queryAllCommitter_gitLabUserAndTwoCommitsAvailableWithDifferentNames_shouldReturnTwoCommitters()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockCommit();
        CommitterDTO committerDTO1 = mockCommitterDTO(commit1);
        Commit commit2 = mockCommit();
        CommitterDTO committerDTO2 = mockCommitterDTO(commit2);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(2));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), containsInAnyOrder(committerDTO1, committerDTO2));
    }

    @Test
    public void queryAllCommitter_gitLabUserAndTwoCommitsAvailableWithSameName_shouldReturnOneCommitter()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockCommit();
        CommitterDTO committerDTO = mockCommitterDTO(commit1);
        Commit commit2 = mockCommit(commit1.getAuthorName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), containsInAnyOrder(
            committerDTOMatcher(committerDTO)
        ));
    }

    @Test
    public void queryAllCommitter_gitHubUser_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION
                                + "?branch=" + branch);

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryAllCommitter_gitHubUserOneCommitExists_shouldReturnOneCommitter() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(0));
    }

    private CommitterDTO mockCommitterDTO(Commit commit) {
        return new CommitterDTO(commit.getAuthorName());
    }

    private void gitLabMockGetBranches(Long repositoryId, RepositoryApi repositoryApi, List<Branch> branches)
        throws GitLabApiException {
        when(repositoryApi.getBranches(repositoryId)).thenReturn(branches);
    }

    private Commit mockCommit() {
        Commit commit = mock(Commit.class);

        CommitStats stats = mock(CommitStats.class);
        when(stats.getAdditions()).thenReturn(Randoms.integer());
        when(stats.getDeletions()).thenReturn(Randoms.integer());

        when(commit.getId()).thenReturn(Randoms.alpha());
        when(commit.getParentIds()).thenReturn(List.of(Randoms.alpha()));
        when(commit.getCommittedDate()).thenReturn(new Date());
        when(commit.getAuthorName()).thenReturn(Randoms.alpha());
        when(commit.getMessage()).thenReturn(Randoms.alpha());
        when(commit.getStats()).thenReturn(stats);
        return commit;
    }

    private Commit mockCommit(String name) {
        Commit commit = mock(Commit.class);

        CommitStats stats = mock(CommitStats.class);
        when(stats.getAdditions()).thenReturn(Randoms.integer());
        when(stats.getDeletions()).thenReturn(Randoms.integer());

        when(commit.getId()).thenReturn(Randoms.alpha());
        when(commit.getParentIds()).thenReturn(List.of(Randoms.alpha()));
        when(commit.getCommittedDate()).thenReturn(new Date());
        when(commit.getAuthorName()).thenReturn(name);
        when(commit.getMessage()).thenReturn(Randoms.alpha());
        when(commit.getStats()).thenReturn(stats);
        return commit;
    }

    private void mockQueryCommits(String branch, GHRepository ghRepository)
        throws IOException {
        GHCommitQueryBuilder commitQueryBuilder = mock(GHCommitQueryBuilder.class);
        when(ghRepository.queryCommits()).thenReturn(commitQueryBuilder);
        when(commitQueryBuilder.from(branch)).thenReturn(commitQueryBuilder);
        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(commitQueryBuilder.list()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(List.of());
    }
}

