package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.StatsDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.SubAssignment;
import io.restassured.response.Response;
import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.junit.Test;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.Matchers.CommitDTOMatcher;
import static utils.Matchers.statsDTOMatcher;

public class RepositoryIntegrationTest extends BaseIntegrationTest {

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";
    private static final String STATS_ENDPOINT_EXTENSION = "stats";
    private static final String COMMITS_ENDPOINT_EXTENSION = "commit";

    @Test
    public void queryStats_gitLabUserAndOneCommitAvailable_shouldReturnCorrectStats() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndTwoCommitsAvailable_shouldReturnCorrectStats() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        Commit commit2 = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(2));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit1),
            statsDTOMatcher(commit2)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndTwoCommitsWithTheSameNameAvailable_shouldReturnCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        Commit commit2 = mockGitLabCommit(commit1.getAuthorName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit1, commit2)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndOneCommitAvailableAndShouldBeMapped_shouldReturnUnmappedCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndTwoCommitsAvailableAndShouldBeMapped_shouldReturnUnmappedCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        Commit commit2 = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(2));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit1),
            statsDTOMatcher(commit2)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndTwoCommitsWithTheSameNameAvailableAndShouldBeMapped_shouldReturnUnmappedCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        Commit commit2 = mockGitLabCommit(commit1.getAuthorName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(commit1, commit2)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndOneCommitAvailableAndShouldBeMapped_shouldReturnMappedCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        Commit commit = mockGitLabCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(key, commit)
        ));
    }

    @Test
    public void queryStats_gitLabUserAndTwoCommitsAvailableAndShouldBeMapped_shouldReturnMappedCorrectStats()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment1 = addSubAssignment(assignment);
        SubAssignment subAssignment2 = addSubAssignment(assignment);

        Commit commit1 = mockGitLabCommit(subAssignment1.getAssignedName());
        Commit commit2 = mockGitLabCommit(subAssignment2.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), containsInAnyOrder(
            statsDTOMatcher(key, commit1, commit2)
        ));
    }

    @Test
    public void queryAllCommits_gitLabUser_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION
                                + "?branch=" + branch);

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryAllCommits_gitLabUserAndOneCommitAvailable_shouldReturnCommit() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit();
        CommitDTO commitDTO = mockCommitDTO(commit);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), containsInAnyOrder(
            CommitDTOMatcher(commitDTO)
        ));
    }

    @Test
    public void queryAllCommits_gitLabUserAndTwoCommitsAvailable_shouldReturnCommits() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        CommitDTO commitDTO1 = mockCommitDTO(commit1);
        Commit commit2 = mockGitLabCommit();
        CommitDTO commitDTO2 = mockCommitDTO(commit2);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), containsInAnyOrder(commitDTO1, commitDTO2));
    }

    @Test
    public void queryAllCommits_gitHubUser_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION
                                + "?branch=" + branch);

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryAllCommits_gitHubUserNoCommitExists_shouldReturnEmptyList() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch);

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(0));
    }

    @Test
    public void queryAllCommits_gitLabUserAndShouldBeMapped_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION
                                + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryAllCommits_gitLabUserAndOneCommitAvailableAndShouldBeMapped_shouldReturnCommit()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        Commit commit = mockGitLabCommit(subAssignment.getAssignedName());
        CommitDTO commitDTO = mockCommitDTO(key, commit);

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);
        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), containsInAnyOrder(
            CommitDTOMatcher(commitDTO)
        ));
    }

    @Test
    public void queryAllCommits_gitLabUserAndTwoCommitsAvailableAndShouldBeMapped_shouldReturnCommits()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit(subAssignment.getAssignedName());
        CommitDTO commitDTO1 = mockCommitDTO(key, commit1);
        Commit commit2 = mockGitLabCommit(subAssignment.getAssignedName());
        CommitDTO commitDTO2 = mockCommitDTO(key, commit2);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), containsInAnyOrder(commitDTO1, commitDTO2));
    }

    @Test
    public void queryAllCommits_gitHubUserAndShouldBeMapped_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        addSubAssignment(assignment);

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION
                                + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryAllCommits_gitHubUserNoCommitExistsAndShouldBeMapped_shouldReturnEmptyList() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION
                                                    + "?branch=" + branch + "&mappedByAssignments=true");

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(0));
    }

    private Commit mockGitLabCommit() {
        return mockGitLabCommit(Randoms.alpha());
    }

    private Commit mockGitLabCommit(String name) {
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

    private CommitDTO mockCommitDTO(Commit commit) {
        return mockCommitDTO(commit.getAuthorName(), commit);
    }

    private CommitDTO mockCommitDTO(String name, Commit commit) {
        return CommitDTO.builder()
                        .author(name)
                        .id(commit.getId())
                        .message(commit.getMessage())
                        .timestamp(commit.getCommittedDate())
                        .parentIds(commit.getParentIds())
                        .isMergeCommit(commit.getParentIds().size() > 1)
                        .additions(commit.getStats().getAdditions())
                        .deletions(commit.getStats().getDeletions())
                        .build();
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
