package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class RepositoryIntegrationTest extends BaseIntegrationTest {

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";

    @Test
    public void queryAllRepositories_userGitHubUserExists_shouldSend200() throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = RestAssured
                                .given()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitHubUserToken)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllRepositories_userGitHubUserExistsAndNoRepositoriesExist_shouldReturnEmptyBody() throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitHubUserToken)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.as(RepositoryDTO[].class).length, is(0));
    }

    // TODO: positive test case for github

    @Test
    public void queryAllRepositories_userLabUserExists_shouldSend200() throws GitLabApiException {
        // Given
        ProjectApi projectApi = gitLabMockProjectApi();
        gitLabMockOwnedProjects(projectApi, List.of());
        gitLabMockMemberProjects(projectApi, List.of());

        // When
        Response response = RestAssured
                                .given()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitLabUserToken)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndNoRepositoriesExist_shouldReturnEmptyBody() throws GitLabApiException {
        // Given
        ProjectApi projectApi = gitLabMockProjectApi();
        gitLabMockOwnedProjects(projectApi, List.of());
        gitLabMockMemberProjects(projectApi, List.of());

        // When
        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitLabUserToken)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.as(RepositoryDTO[].class).length, is(0));
    }

    @Test
    public void queryAllRepositories_userGitLabUserExistsAndOneMemberAndOneOwnedRepositoryExist_shouldReturn2Elements() throws GitLabApiException {
        // Given
        Project memberedProject = gitLabCreateRandomProject();
        Project ownedProject = gitLabCreateRandomProject();

        ProjectApi projectApi = gitLabMockProjectApi();
        gitLabMockOwnedProjects(projectApi, List.of(memberedProject));
        gitLabMockMemberProjects(projectApi, List.of(ownedProject));

        // When
        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitLabUserToken)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.as(RepositoryDTO[].class).length, is(2));
        RepositoryDTO[] repositories = response.as(RepositoryDTO[].class);

        assertThat(Arrays.asList(repositories), containsInAnyOrder(
            new RepositoryDTO(memberedProject.getId(), memberedProject.getName(), memberedProject.getHttpUrlToRepo()),
            new RepositoryDTO(ownedProject.getId(), ownedProject.getName(), ownedProject.getHttpUrlToRepo())
        ));
    }
}

