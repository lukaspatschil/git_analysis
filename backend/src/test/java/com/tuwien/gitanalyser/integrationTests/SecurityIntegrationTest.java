package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.RefreshAccessTokenDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RefreshAuthenticationDTO;
import com.tuwien.gitanalyser.endpoints.dtos.RepositoryDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class SecurityIntegrationTest extends BaseIntegrationTest {

    @Test
    public void queryGitLabLoginURL_always_shouldRedirectAndReturnStatusCode3xx() {

        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .redirects().follow(false)
                                .when().get(GITLAB_LOGIN_ENDPOINT)
                                .then().extract().response();

        assertThat(response.statusCode(), equalTo(HttpStatus.MOVED_TEMPORARILY.value()));
    }

    @Test
    public void queryGitHubLoginURL_always_shouldRedirectAndReturnStatusCode3xx() {

        Response response = RestAssured
                                .given()
                                .contentType(ContentType.JSON)
                                .redirects().follow(false)
                                .when()
                                .get(GITHUB_LOGIN_ENDPOINT)
                                .then()
                                .extract().response();

        assertThat(response.getStatusCode(), is(HttpStatus.MOVED_TEMPORARILY.value()));
    }

    @Test
    public void queryGitLabLoginURL_always_shouldRedirectAndSend200() {

        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .redirects().follow(true)
                                .when().get(GITLAB_LOGIN_ENDPOINT)
                                .then().extract().response();

        assertThat(response.statusCode(), Matchers.either(
            Matchers.is(HttpStatus.SERVICE_UNAVAILABLE.value())).or(
            Matchers.is(HttpStatus.OK.value())).or(
            Matchers.is(HttpStatus.FORBIDDEN.value())
        ));
    }

    @Test
    public void queryGitHubLoginURL_always_shouldRedirect() {

        Response response = RestAssured
                                .given()
                                .contentType(ContentType.JSON)
                                .redirects().follow(true)
                                .when()
                                .get(GITHUB_LOGIN_ENDPOINT)
                                .then()
                                .extract().response();

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitLabLoginGiven_shouldReturn200() {

        Response response = refreshAccessToken(gitLabRefreshToken, gitLabFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitHubLoginGiven_shouldReturn200() {

        Response response = refreshAccessToken(gitHubRefreshToken, gitHubFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitLabLoginGiven_shouldReturnCorrectData() {

        Response response = refreshAccessToken(gitLabRefreshToken, gitLabFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.getCookie("fingerprint"), not(equalTo(gitLabFingerprint)));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);
        assertThat(result.getRefreshToken(), not(equalTo(gitLabAccessToken)));
        assertThat(result.getAccessToken(), not(equalTo(gitLabRefreshToken)));
    }

    @Test
    public void queryRefreshAccessToken_gitHubLoginGiven_shouldReturnCorrectData() {

        Response response = refreshAccessToken(gitHubRefreshToken, gitHubFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.getCookie("fingerprint"), not(equalTo(gitLabFingerprint)));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);
        assertThat(result.getRefreshToken(), not(equalTo(gitHubAccessToken)));
        assertThat(result.getAccessToken(), not(equalTo(gitHubRefreshToken)));
    }

    @Test
    public void queryRefreshAccessToken_gitLabLoginAndRefreshed_shouldReturn200() {

        Response response = refreshAccessToken(gitLabRefreshToken, gitLabFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);

        Response response2 = refreshAccessToken(result.getRefreshToken(), response.getCookie("fingerprint"));

        assertThat(response2.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitHubLoginAndRefreshed_shouldReturn200() {

        Response response = refreshAccessToken(gitHubRefreshToken, gitHubFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);

        Response response2 = refreshAccessToken(result.getRefreshToken(), response.getCookie("fingerprint"));

        assertThat(response2.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryUser_gitLabLoginAndRefreshed_shouldReturn200() {

        Response response = refreshAccessToken(gitLabRefreshToken, gitLabFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);

        Response response2 = callGetRestEndpoint("Bearer " + result.getAccessToken(), USER_ENDPOINT);

        assertThat(response2.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryUser_gitHubLoginAndRefreshed_shouldReturn200() {

        Response response = refreshAccessToken(gitHubRefreshToken, gitHubFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        RefreshAuthenticationDTO result = response.as(RefreshAuthenticationDTO.class);

        Response response2 = callGetRestEndpoint("Bearer " + result.getAccessToken(), USER_ENDPOINT);

        assertThat(response2.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitLabLoginAndWrongFingerPrint_shouldReturn401() {

        Response response = refreshAccessToken(gitLabRefreshToken, gitHubFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void queryRefreshAccessToken_gitHubLoginAndWrongFingerPrint_shouldReturn401() {

        Response response = refreshAccessToken(gitHubRefreshToken, gitLabFingerprint);

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void queryAllRepositories_withoutAuthentication_shouldSend401() {
        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void queryAllRepositories_withAuthentication_shouldEnableCors() throws IOException {
        // Given
        gitHubMockAPI();

        // When
        Response response = RestAssured
                                .given()
                                .log().all()
                                .contentType(ContentType.JSON)
                                .header(HttpHeaders.AUTHORIZATION, gitHubUserToken)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                                .header(HttpHeaders.ORIGIN, "https://www.someurl.com")
                                .when().get(REPOSITORY_ENDPOINT)
                                .then().extract().response();

        // Then
        assertThat(response.as(RepositoryDTO[].class).length, is(0));
        assertThat(response.getHeaders().hasHeaderWithName(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
                   equalTo(true));
    }

    private Response refreshAccessToken(String refreshToken, String fingerprint) {
        return RestAssured
                   .given()
                   .log().all()
                   .contentType(ContentType.JSON)
                   .body(new RefreshAccessTokenDTO(refreshToken))
                   .cookie("fingerprint", fingerprint)
                   .when()
                   .post(REFRESH_ENDPOINT)
                   .then()
                   .extract().response();
    }
}
