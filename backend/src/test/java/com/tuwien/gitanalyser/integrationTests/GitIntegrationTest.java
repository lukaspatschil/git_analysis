package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RepositoryDTO;
import com.tuwien.gitanalyser.entity.Repository;
import io.restassured.response.Response;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.junit.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.http.HttpStatus;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static utils.Matchers.branchDTOMatcher;
import static utils.Matchers.repositoryMatcher;

public class GitIntegrationTest extends BaseIntegrationTest {

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
    public void queryAllRepositories_userGitLabUserExistsAndOneMemberProjectAndOneRepositoryInDatabase_shouldDeleteOtherRepositories()
        throws GitLabApiException {
        // Given
        Project memberedProject = gitLabCreateRandomProject();

        Repository repository = addRepository(gitLabUser, Randoms.getLong());

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockOwnedProjects(projectApi, List.of(memberedProject));

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK.value()));
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

    private void gitLabMockGetBranches(Long repositoryId, RepositoryApi repositoryApi, List<Branch> branches)
        throws GitLabApiException {
        when(repositoryApi.getBranches(repositoryId)).thenReturn(branches);
    }

}

