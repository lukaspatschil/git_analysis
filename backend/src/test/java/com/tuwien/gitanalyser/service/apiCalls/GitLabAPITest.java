package com.tuwien.gitanalyser.service.apiCalls;

import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.service.apiCalls.factory.GitLabAPIFactory;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        mockApi(emptyList(), emptyList());

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, is(empty()));
    }

    @Test
    void getAllRepositories_OneOwnedProject_singleItemAsList() throws GitLabApiException, GitLabException {
        // Given
        mockApi(List.of(FIRST_PROJECT), emptyList());

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
        mockApi(emptyList(), List.of(THIRD_PROJECT));

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
        mockApi(List.of(FIRST_PROJECT), List.of(THIRD_PROJECT));

        // When
        var result = sut.getAllRepositories(accessToken);

        // Then
        assertThat(result, contains(
            hasProperty("platformId", is(1L)),
            hasProperty("platformId", is(3L))
        ));
    }

    @Test
    void getAllRepositories_TwoOwnedProjectAndTWOMembershipProject_fourItemInList() throws GitLabApiException,
                                                                                           GitLabException {
        // Given
        mockApi(List.of(FIRST_PROJECT, SECOND_PROJECT), List.of(THIRD_PROJECT, FOURTH_PROJECT));

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
    void getRepositoryById_gitlabLibraryThrowsException_sutThrowsException() throws GitLabApiException {
        // Given
        long exceptionRepositoryId = Randoms.getLong();

        GitLabApi api = mockFactory();
        ProjectApi projectApi = mockProjectApi(api);
        when(projectApi.getProject(exceptionRepositoryId)).thenThrow(GitLabApiException.class);

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
        ProjectApi projectApi = mockProjectApi(api);
        when(projectApi.getProject(existingRepositoryId)).thenReturn(project);

        // When
        NotSavedRepositoryInternalDTO result = sut.getRepositoryById(accessToken,
                                                                     existingRepositoryId);

        // Then
        assertThat(result, hasProperty("platformId", is(existingRepositoryId)));
    }

    private void mockApi(List<Project> ownedProjects,
                         List<Project> memberProjects) throws GitLabApiException {
        GitLabApi api = mockFactory();
        ProjectApi projectApi = mockProjectApi(api);
        mockOwnedProjects(projectApi, ownedProjects);
        mockMemberProjects(projectApi, memberProjects);
    }

    private GitLabApi mockFactory() {
        GitLabApi api = mock(GitLabApi.class);
        when(gitLabAPIFactory.createObject(accessToken)).thenReturn(api);
        return api;
    }

    private void mockOwnedProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getOwnedProjects()).thenReturn(projects);
    }

    private void mockMemberProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getMemberProjects()).thenReturn(projects);
    }

    private ProjectApi mockProjectApi(GitLabApi api) {
        ProjectApi projectApi = mock(ProjectApi.class);
        when(api.getProjectApi()).thenReturn(projectApi);
        return projectApi;
    }
}