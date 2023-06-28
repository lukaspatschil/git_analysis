package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.Matchers.commitDTOMatcher;
import static utils.Matchers.committerDTOMatcher;
import static utils.Matchers.statsDTOMatcher;

public class RepositoryIntegrationTest extends BaseIntegrationTest {

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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(2));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch, "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch, "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(2));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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

        SubAssignment subAssignment = prepareAssignment(repositoryId, key);

        Commit commit = mockGitLabCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch, "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                                                    + STATS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch, "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(StatsDTO[].class).length, equalTo(1));
        StatsDTO[] stats = response.as(StatsDTO[].class);

        assertThat(Arrays.asList(stats), Matchers.containsInAnyOrder(
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
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch));

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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
            commitDTOMatcher(commitDTO)
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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits),
                   Matchers.containsInAnyOrder(commitDTOMatcher(commitDTO1,
                                                                commitDTO1.getAdditions() - commitDTO1.getDeletions()),
                                               commitDTOMatcher(commitDTO2,
                                                       commitDTO1.getAdditions() - commitDTO1.getDeletions()
                                                           + commitDTO2.getAdditions() - commitDTO2.getDeletions())));
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
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch));

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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch));

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
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true"));

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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of(
                                                    "branch", branch,
                                                    "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
            commitDTOMatcher(commitDTO)
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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
                       commitDTOMatcher(commitDTO1),
                       commitDTOMatcher(commitDTO2)
                   )
        );
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
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true"));

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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(0));
    }

    @Test
    public void queryAllCommits_gitLabUserAndCommitterNameGiven_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String committerName = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "committerName", committerName));

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryAllCommits_gitLabUserAndOneCommitAvailableAndCommitterNameEqualsAuthorName_shouldReturnCommit()
        throws GitLabApiException {
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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "committerName", commit.getAuthorName()));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
            commitDTOMatcher(commitDTO)
        ));
    }

    @Test
    public void queryAllCommits_gitLabUserAndTwoCommitsAvailableAndCommitterNameEqualToFirstCommitAuthor_shouldReturnOneCommit()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit();
        CommitDTO commitDTO1 = mockCommitDTO(commit1);
        Commit commit2 = mockGitLabCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "committerName", commit1.getAuthorName()));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits),
                   Matchers.containsInAnyOrder(commitDTOMatcher(commitDTO1,
                                                                commitDTO1.getAdditions() - commitDTO1.getDeletions())));
    }

    @Test
    public void queryAllCommits_gitLabUserAndTwoCommitsAvailableAndCommitterNameGiven_shouldReturnTwoCommit()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String committerName = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit1 = mockGitLabCommit(committerName);
        CommitDTO commitDTO1 = mockCommitDTO(commit1);
        Commit commit2 = mockGitLabCommit(committerName);
        CommitDTO commitDTO2 = mockCommitDTO(commit2);

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "committerName", committerName));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits),
                   Matchers.containsInAnyOrder(commitDTOMatcher(commitDTO1,
                                                                commitDTO1.getAdditions() - commitDTO1.getDeletions()),
                                               commitDTOMatcher(commitDTO2,
                                                       commitDTO1.getAdditions() - commitDTO1.getDeletions()
                                                           + commitDTO2.getAdditions() - commitDTO2.getDeletions())
                   ));
    }

    @Test
    public void queryAllCommits_gitHubUserAndCommitterNameGiven_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String committerName = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "committerName", committerName));

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryAllCommits_gitHubUserNoCommitExistsAndCommitterNameGiven_shouldReturnEmptyList()
        throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String committerName = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "committerName", committerName));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(0));
    }

    @Test
    public void queryAllCommits_gitLabUserAndShouldBeMappedAndCommitterNameGiven_shouldCallGitLabService()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();
        String committerName = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockGitLabCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true",
                                   "committerName", committerName));

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryAllCommits_gitLabUserAndOneCommitAvailableAndShouldBeMappedAndCommitterNameGiven_shouldReturnCommit()
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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true",
                                                       "committerName", key));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(1));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
            commitDTOMatcher(commitDTO)
        ));
    }

    @Test
    public void queryAllCommits_gitLabUserAndTwoCommitsAvailableAndShouldBeMappedAndCommitterNameGiven_shouldReturnCommits()
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
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true",
                                                       "committerName", key));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(2));
        CommitDTO[] commits = response.as(CommitDTO[].class);

        assertThat(Arrays.asList(commits), Matchers.containsInAnyOrder(
                       commitDTOMatcher(commitDTO1),
                       commitDTOMatcher(commitDTO2)
                   )
        );
    }

    @Test
    public void queryAllCommits_gitHubUserAndShouldBeMappedAndCommitterNameGiven_shouldCallGitHubService()
        throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();
        String committerName = Randoms.alpha();

        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        addSubAssignment(assignment);

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITS_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true",
                                   "committerName", committerName));

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryAllCommits_gitHubUserNoCommitExistsAndShouldBeMappedAndCommitterNameGiven_shouldReturnEmptyList()
        throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String committerName = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITS_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true",
                                                       "committerName", committerName));

        // Then
        assertThat(response.as(CommitDTO[].class).length, equalTo(0));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldNotBeMapped_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockCommit();
        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION,
                            Map.of("branch", branch));

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryCommitter_gitLabUserAndOneCommitAvailableAndShouldNotBeMapped_shouldReturnCommitter()
        throws GitLabApiException {
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
                                                    + COMMITTER_ENDPOINT_EXTENSION, Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), containsInAnyOrder(equalTo(committerDTO)));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldNotBeMappedAndTwoCommitsAvailableWithDifferentNames_shouldReturnTwoCommitters()
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
                                                    + COMMITTER_ENDPOINT_EXTENSION, Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(2));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), containsInAnyOrder(committerDTO1, committerDTO2));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldNotBeMappedAndTwoCommitsAvailableWithSameName_shouldReturnOneCommitter()
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
                                                    + COMMITTER_ENDPOINT_EXTENSION, Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        MatcherAssert.assertThat(Arrays.asList(committers), Matchers.containsInAnyOrder(
            utils.Matchers.committerDTOMatcher(committerDTO)
        ));
    }

    @Test
    public void queryCommitter_gitHubUserAndShouldNotBeMapped_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION,
                            Map.of("branch", branch));

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryCommitter_gitHubUserAndShouldNotBeMappedAndNoCommitExists_shouldReturnEmptyArray()
        throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION, Map.of("branch", branch));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(0));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldBeMapped_shouldCallGitLabService() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockCommit();
        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        callGetRestEndpoint(gitLabUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true"));

        // Then
        verify(gitLabApi).getCommitsApi();
    }

    @Test
    public void queryCommitter_gitLabUserAndOneCommitAvailableAndAssignmentAvailableAndShouldBeMapped_shouldReturnMappedCommitter()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        SubAssignment subAssignment = prepareAssignment(repositoryId, key);

        Commit commit = mockCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), Matchers.containsInAnyOrder(committerDTOMatcher(key)));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldBeMappedAndTwoCommitsAvailableWithDifferentNamesAndOneShouldBeMapped_shouldReturnTwoCommitters()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        SubAssignment subAssignment = prepareAssignment(repositoryId, key);

        Commit commit1 = mockCommit(subAssignment.getAssignedName());
        Commit commit2 = mockCommit();

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(2));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), Matchers.containsInAnyOrder(committerDTOMatcher(key),
                                                                          committerDTOMatcher(commit2.getAuthorName())));
    }

    @Test
    public void queryCommitter_gitLabUserAndShouldBeMappedAndTwoCommitsAvailableWithSameName_shouldReturnOneCommitter()
        throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();
        String key = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        SubAssignment subAssignment = prepareAssignment(repositoryId, key);

        Commit commit1 = mockCommit(subAssignment.getAssignedName());
        Commit commit2 = mockCommit(subAssignment.getAssignedName());

        gitLabMockGetCommits(commitsApi, repositoryId, branch, commit1, commit2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(1));
        CommitterDTO[] committers = response.as(CommitterDTO[].class);

        assertThat(Arrays.asList(committers), Matchers.containsInAnyOrder(
            committerDTOMatcher(key)
        ));
    }

    @Test
    public void queryCommitter_gitHubUserAndShouldBeMapped_shouldCallGitHubService() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        gitHubMockGHRepository(gitHubApi, repositoryId);

        // When
        callGetRestEndpoint(gitHubUserToken,
                            REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + COMMITTER_ENDPOINT_EXTENSION,
                            Map.of("branch", branch,
                                   "mappedByAssignments", "true"));

        // Then
        verify(gitHubApi).getRepositoryById(repositoryId);
    }

    @Test
    public void queryCommitter_gitHubUserAndShouldBeMappedAndNoCommitExists_shouldReturnEmptyArray()
        throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitHub gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(gitHubApi, repositoryId);

        mockQueryCommits(branch, ghRepository);

        // When
        Response response = callGetRestEndpoint(gitHubUserToken,
                                                REPOSITORY_ENDPOINT + "/" + repositoryId + "/"
                                                    + COMMITTER_ENDPOINT_EXTENSION,
                                                Map.of("branch", branch,
                                                       "mappedByAssignments", "true"));

        // Then
        assertThat(response.as(CommitterDTO[].class).length, equalTo(0));
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

    private CommitterDTO mockCommitterDTO(Commit commit) {
        return new CommitterDTO(commit.getAuthorName());
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

    private SubAssignment prepareAssignment(long repositoryId, String key) {
        Repository repository = addRepository(gitLabUser, repositoryId);
        Assignment assignment = addAssignment(key, repository);
        return addSubAssignment(assignment);
    }
}
