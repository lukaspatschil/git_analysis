package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.DTOs.BranchDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;
import io.restassured.response.Response;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.junit.Test;
import org.kohsuke.github.GHRepository;
import org.springframework.http.HttpStatus;
import utils.Randoms;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryIntegrationTest extends BaseIntegrationTest {

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";

    private static final String BRANCHES_ENDPOINT_EXTENSION = "branch";


    // TODO: positive test case for github
    private static void gitLabMockGetBranches(Long repositoryId, RepositoryApi repositoryApi, List<Branch> branches)
        throws GitLabApiException {
        when(repositoryApi.getBranches(repositoryId)).thenReturn(branches);
    }

    @Test
    public void queryAllRepositories_userGitHubUserExists_shouldSend200() throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = callRestEndpoint(gitHubUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllRepositories_userGitHubUserExistsAndNoRepositoriesExist_shouldReturnEmptyList()
        throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = callRestEndpoint(gitHubUserToken, REPOSITORY_ENDPOINT);

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
        Response response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

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
        Response response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

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
        Response response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT);

        // Then
        assertThat(response.as(NotSavedRepositoryDTO[].class).length, is(2));
        NotSavedRepositoryDTO[] repositories = response.as(NotSavedRepositoryDTO[].class);

        assertThat(Arrays.asList(repositories), containsInAnyOrder(
            new NotSavedRepositoryDTO(ownedProject.getId(),
                                      ownedProject.getName(),
                                      ownedProject.getHttpUrlToRepo()),
            new NotSavedRepositoryDTO(memberedProject.getId(),
                                      memberedProject.getName(),
                                      memberedProject.getHttpUrlToRepo())
        ));
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
        Response response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT + "/" + project.getId());

        // Then
        RepositoryDTO repositories = response.as(RepositoryDTO.class);

        assertThat(repositories, equalTo(
            new RepositoryDTO(project.getId(),
                              project.getName(),
                              project.getHttpUrlToRepo())
        ));
    }

    @Test
    public void queryRepositoriesById_userGitLabUserExistsProjectWithIdExists_shouldBeAddedToRepositories()
        throws GitLabApiException {
        // Given
        Project project = gitLabCreateRandomProject();

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockGetProject(projectApi, project);

        // When
        Response response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT + "/" + project.getId());

        RepositoryDTO repositories = response.as(RepositoryDTO.class);

        assertThat(repositories, equalTo(
            new RepositoryDTO(project.getId(),
                              project.getName(),
                              project.getHttpUrlToRepo())
        ));

        // Then
        SavedRepository byUserIdAndPlatformId =
            repositoryRepository.findByUserIdAndPlatformId(gitLabUser.getId(), project.getId());

        assertThat(byUserIdAndPlatformId.getUser().getId(), equalTo(gitLabUser.getId()));
    }

    @Test
    public void queryRepositoriesById_userGitLabUserExistsProjectWithIdExistsAndAlreadyCloned_notBeClonedAgain()
        throws GitLabApiException {
        // Given
        Project project = gitLabCreateRandomProject();

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = gitLabMockProjectApi(gitLabApi);
        gitLabMockGetProject(projectApi, project);

        Response response = callRestEndpoint(gitLabUserToken,
                                             REPOSITORY_ENDPOINT + "/" + project.getId());

        RepositoryDTO repository = response.as(RepositoryDTO.class);

        assertThat(repository, equalTo(
            new RepositoryDTO(project.getId(),
                              project.getName(),
                              project.getHttpUrlToRepo())
        ));

        // When
        response = callRestEndpoint(gitLabUserToken, REPOSITORY_ENDPOINT + "/" + project.getId());

        repository = response.as(RepositoryDTO.class);

        assertThat(repository, equalTo(
            new RepositoryDTO(project.getId(),
                              project.getName(),
                              project.getHttpUrlToRepo())
        ));

        // Then
        assertThat(repositoryRepository.findAll().size(), is(1));
        verify(jGit, times(1)).cloneRepository(eq(repository.getUrl()), any(), eq(gitLabUser.getAccessToken()));
    }

    @Test
    public void queryAllBranches_userGitHubUserExists_shouldSend200() throws IOException {
        // Given
        Long repositoryId = Randoms.getLong();

        var gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(repositoryId, gitHubApi);
        gitHubMockBranches(ghRepository);
        // When
        Response response = callRestEndpoint(
            gitHubUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }


    // TODO: positive test case for github
    @Test
    public void queryAllBranches_userGitHubUserExistsAndNoBranchesExist_shouldReturnEmptyList() throws IOException {
        // Given
        Long repositoryId = Randoms.getLong();

        var gitHubApi = gitHubMockFactory();
        GHRepository ghRepository = gitHubMockGHRepository(repositoryId, gitHubApi);
        gitHubMockBranches(ghRepository);

        // When
        Response response = callRestEndpoint(
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
        Response response = callRestEndpoint(
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
        Response response = callRestEndpoint(
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
        Response response = callRestEndpoint(
            gitLabUserToken, REPOSITORY_ENDPOINT + "/" + repositoryId + "/" + BRANCHES_ENDPOINT_EXTENSION);

        // Then
        assertThat(response.as(BranchDTO[].class).length, equalTo(2));
        BranchDTO[] repositories = response.as(BranchDTO[].class);

        assertThat(Arrays.asList(repositories), containsInAnyOrder(
            new BranchDTO(branch1.getName()),
            new BranchDTO(branch2.getName())
        ));
    }
}

