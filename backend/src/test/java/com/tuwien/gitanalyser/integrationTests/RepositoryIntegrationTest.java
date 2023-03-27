package com.tuwien.gitanalyser.integrationTests;

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
import utils.Randoms;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.Matchers.statsDTOMatcher;

public class RepositoryIntegrationTest extends BaseIntegrationTest {

    private static final String STATS_ENDPOINT_EXTENSION = "stats";
    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";

    @Test
    public void queryStats_gitLabUserAndOneCommitAvailable_shouldReturnCorrectStats() throws GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();
        String branch = Randoms.alpha();

        GitLabApi gitLabApi = gitLabMockFactory();
        CommitsApi commitsApi = gitLabMockCommitsApi(gitLabApi);

        Commit commit = mockCommit();

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

        Commit commit1 = mockCommit();
        Commit commit2 = mockCommit();

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

        Commit commit1 = mockCommit();
        Commit commit2 = mockCommit(commit1.getAuthorName());

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

        Commit commit = mockCommit();

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

        Commit commit1 = mockCommit();
        Commit commit2 = mockCommit();

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

        Commit commit1 = mockCommit();
        Commit commit2 = mockCommit(commit1.getAuthorName());

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

        Commit commit = mockCommit(subAssignment.getAssignedName());

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

        Commit commit1 = mockCommit(subAssignment1.getAssignedName());
        Commit commit2 = mockCommit(subAssignment2.getAssignedName());

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
}
