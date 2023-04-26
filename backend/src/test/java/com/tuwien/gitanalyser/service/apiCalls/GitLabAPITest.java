package com.tuwien.gitanalyser.service.apiCalls;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.service.apiCalls.factory.GitLabAPIFactory;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.Matchers.branchInternalDTOMatcher;

class GitLabAPITest {

    private static final Project FIRST_PROJECT = new Project();
    private static final Project SECOND_PROJECT = new Project();
    private static final Project THIRD_PROJECT = new Project();
    private static final Project FOURTH_PROJECT = new Project();
    private GitLabAPI sut;
    private GitLabAPIFactory gitLabAPIFactory;
    private String accessToken;

    @BeforeEach
    void setUp() {
        gitLabAPIFactory = mock(GitLabAPIFactory.class);
        sut = new GitLabAPI(gitLabAPIFactory);

        accessToken = Randoms.alpha();

        FIRST_PROJECT.setId(1L);
        SECOND_PROJECT.setId(2L);
        THIRD_PROJECT.setId(3L);
        FOURTH_PROJECT.setId(4L);
    }

    @Test
    void getAllRepositories_noRepositoriesAvailable_returnEmptyList()
        throws GitLabApiException, GitLabException {
        // Given
        prepareProjectApi(emptyList(), emptyList());

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, is(empty()));
    }

    @Test
    void getAllRepositories_OneOwnedProject_singleItemAsList() throws GitLabApiException, GitLabException {
        // Given
        prepareProjectApi(List.of(FIRST_PROJECT), emptyList());

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("platformId", is(1L))
        ));
    }

    @Test
    void getAllRepositories_OneMembershipProject_singleItemAsList()
        throws GitLabApiException, GitLabException {
        // Given
        prepareProjectApi(emptyList(), List.of(THIRD_PROJECT));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("platformId", is(3L))
        ));
    }

    @Test
    void getAllRepositories_OneOwnedProjectAndOneMembershipProject_twoItemInList() throws GitLabApiException,
                                                                                          GitLabException {
        // Given
        prepareProjectApi(List.of(FIRST_PROJECT), List.of(THIRD_PROJECT));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("platformId", is(1L)),
            hasProperty("platformId", is(3L))
        ));
    }

    @Test
    void getAllRepositories_TwoOwnedProjectAndTwoMembershipProject_fourItemInList() throws GitLabApiException,
                                                                                           GitLabException {
        // Given
        prepareProjectApi(List.of(FIRST_PROJECT, SECOND_PROJECT), List.of(THIRD_PROJECT, FOURTH_PROJECT));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("platformId", is(1L)),
            hasProperty("platformId", is(2L)),
            hasProperty("platformId", is(3L)),
            hasProperty("platformId", is(4L))
        ));
    }

    @Test
    void getAllRepositories_ownedProjectThrowsException_throwsGitLabException() throws GitLabApiException {
        // Given
        prepareOwnedProjectsThrowsException();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllRepositories(accessToken));
    }

    @Test
    void getAllRepositories_memberProjectThrowsException_throwsGitLabException() throws GitLabApiException {
        // Given
        prepareMemberProjectsThrowsException();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllRepositories(accessToken));
    }

    @Test
    void getAllRepositories_factoryThrowsIOException_throwsGitLabException() {
        // Given
        mockFactoryThrows();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllRepositories(accessToken));
    }

    @Test
    void getAllBranches_noRepositoriesAvailable_returnEmptyList()
        throws GitLabApiException, GitLabException {
        // Given
        Long platformId = Randoms.getLong();

        GitLabApi api = mockFactory();
        RepositoryApi repositoryApi = prepareRepositoryAPI(api);
        prepareGetBranches(platformId, repositoryApi, List.of());

        // When
        var result = sut.getAllBranches(accessToken, platformId);

        // Then
        assertThat(result, is(empty()));
    }

    @Test
    void getAllBranches_factoryThrowsIOException_throwsGitLabException() {
        // Given
        mockFactoryThrows();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllBranches(accessToken, Randoms.getLong()));
    }

    @Test
    void getAllBranches_getBranchesThrowsGitLabException_throwsGitLabException() throws GitLabApiException {
        // Given
        Long platformId = Randoms.getLong();

        GitLabApi api = mockFactory();
        RepositoryApi repositoryApi = prepareRepositoryAPI(api);
        when(repositoryApi.getBranches(platformId)).thenThrow(GitLabApiException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllBranches(accessToken, platformId));
    }

    @Test
    void getAllBranches_oneBranchAvailable_singleItemAsList() throws GitLabApiException, GitLabException {
        // Given
        Long platformId = Randoms.getLong();

        GitLabApi api = mockFactory();
        RepositoryApi repositoryApi = prepareRepositoryAPI(api);
        Branch branchMock = createRandomBranchMock();
        prepareGetBranches(platformId, repositoryApi, List.of(branchMock));

        // When
        List<BranchInternalDTO> result = sut.getAllBranches(accessToken, platformId);

        // Then
        assertThat(result, contains(
            branchInternalDTOMatcher(branchMock)
        ));
    }

    @Test
    void getAllBranches_twoBranchesAvailable_twoItemInList() throws GitLabApiException,
                                                                    GitLabException {
        // Given
        Long platformId = Randoms.getLong();

        GitLabApi api = mockFactory();
        RepositoryApi repositoryApi = prepareRepositoryAPI(api);
        Branch branchMock1 = createRandomBranchMock();
        Branch branchMock2 = createRandomBranchMock();
        prepareGetBranches(platformId, repositoryApi, List.of(branchMock1, branchMock2));

        // When
        var result = sut.getAllBranches(accessToken, platformId);

        // Then
        assertThat(result, containsInAnyOrder(
            branchInternalDTOMatcher(branchMock1),
            branchInternalDTOMatcher(branchMock2)
        ));
    }

    @Test
    void getAllBranches_threeBranchesAvailable_threeItemsInList() throws GitLabApiException,
                                                                         GitLabException {
        // Given
        Long platformId = Randoms.getLong();

        GitLabApi api = mockFactory();
        RepositoryApi repositoryApi = prepareRepositoryAPI(api);
        Branch branchMock1 = createRandomBranchMock();
        Branch branchMock2 = createRandomBranchMock();
        Branch branchMock3 = createRandomBranchMock();
        prepareGetBranches(platformId, repositoryApi, List.of(branchMock1, branchMock2, branchMock3));

        // When
        var result = sut.getAllBranches(accessToken, platformId);

        // Then
        assertThat(result, containsInAnyOrder(
            branchInternalDTOMatcher(branchMock1),
            branchInternalDTOMatcher(branchMock2),
            branchInternalDTOMatcher(branchMock3)
        ));
    }

    @Test
    void getAllCommits_factoryThrowsIOException_throwsGitLabException() {
        // Given
        mockFactoryThrows();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getAllCommits(accessToken, Randoms.getLong(), Randoms.alpha()));
    }

    @Test
    void getRepositoryById_gitlabLibraryThrowsException_sutThrowsException() throws GitLabApiException {
        // Given
        long exceptionRepositoryId = Randoms.getLong();

        GitLabApi api = mockFactory();
        ProjectApi projectApi = prepareProjectApi(api);
        when(projectApi.getProject(exceptionRepositoryId)).thenThrow(GitLabApiException.class);

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getRepositoryById(accessToken, exceptionRepositoryId));
    }

    @Test
    void getRepositoryById_gitlabFactoryThrowsIOException_sutThrowsException() {
        // Given
        long exceptionRepositoryId = Randoms.getLong();

        // When
        mockFactoryThrows();

        // When + Then
        assertThrows(GitLabException.class, () -> sut.getRepositoryById(accessToken, exceptionRepositoryId));
    }

    @Test
    void getRepositoryById_gitlabLibraryReturnsOneRepository_sutReturnsRepository()
        throws GitLabApiException, GitLabException {
        // Given
        Long existingRepositoryId = Randoms.getLong();

        Project project = new Project();
        project.setId(existingRepositoryId);

        GitLabApi api = mockFactory();
        ProjectApi projectApi = prepareProjectApi(api);
        when(projectApi.getProject(existingRepositoryId)).thenReturn(project);

        // When
        NotSavedRepositoryInternalDTO result = sut.getRepositoryById(accessToken,
                                                                     existingRepositoryId);

        // Then
        assertThat(result, hasProperty("platformId", is(existingRepositoryId)));
    }

    private void prepareProjectApi(List<Project> ownedProjects,
                                   List<Project> memberProjects) throws GitLabApiException {
        GitLabApi api = mockFactory();
        ProjectApi projectApi = prepareProjectApi(api);
        mockOwnedProjects(projectApi, ownedProjects);
        mockMemberProjects(projectApi, memberProjects);
    }

    private void prepareOwnedProjectsThrowsException() throws GitLabApiException {
        GitLabApi api = mockFactory();
        ProjectApi projectApi = prepareProjectApi(api);
        when(projectApi.getOwnedProjects()).thenThrow(GitLabApiException.class);
    }

    private void prepareMemberProjectsThrowsException() throws GitLabApiException {
        GitLabApi api = mockFactory();
        ProjectApi projectApi = prepareProjectApi(api);
        when(projectApi.getMemberProjects()).thenThrow(GitLabApiException.class);
    }

    private GitLabApi mockFactory() {
        GitLabApi api = mock(GitLabApi.class);
        when(gitLabAPIFactory.createObject(accessToken)).thenReturn(api);
        return api;
    }

    private GitLabApi mockFactoryThrows() {
        GitLabApi api = mock(GitLabApi.class);
        when(gitLabAPIFactory.createObject(accessToken)).thenThrow(IOException.class);
        return api;
    }

    private void mockOwnedProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getOwnedProjects()).thenReturn(projects);
    }

    private void mockMemberProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getMemberProjects()).thenReturn(projects);
    }

    private ProjectApi prepareProjectApi(GitLabApi api) {
        ProjectApi projectApi = mock(ProjectApi.class);
        when(api.getProjectApi()).thenReturn(projectApi);
        return projectApi;
    }

    private RepositoryApi prepareRepositoryAPI(GitLabApi api) {
        RepositoryApi repositoryApi = mock(RepositoryApi.class);
        when(api.getRepositoryApi()).thenReturn(repositoryApi);
        return repositoryApi;
    }

    private Branch createRandomBranchMock() {
        Branch branchMock2 = mock(Branch.class);
        when(branchMock2.getName()).thenReturn(Randoms.alpha());
        return branchMock2;
    }

    private void prepareGetBranches(Long platformId, RepositoryApi repositoryApi, List<Branch> branchMocks)
        throws GitLabApiException {
        when(repositoryApi.getBranches(platformId)).thenReturn(branchMocks);
    }
}