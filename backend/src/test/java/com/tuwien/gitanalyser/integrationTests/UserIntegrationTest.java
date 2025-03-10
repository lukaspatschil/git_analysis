package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.UserDTO;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class UserIntegrationTest extends BaseIntegrationTest {

    @Test
    public void queryUser_userGitHubUserExists_shouldSend200() {
        // Given

        // When
        Response response = callGetRestEndpoint(gitHubUserToken, USER_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryUser_userGitLabUserExists_shouldSend200() {
        // Given

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, USER_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void queryUser_userGitHubUserExists_shouldSendCorrectUser() {
        // Given
        UserDTO expectedUser = new UserDTO(gitHubUser.getId(), gitHubUser.getUsername(),
                                           gitHubUser.getEmail(), gitHubUser.getPictureUrl());

        // When
        Response response = callGetRestEndpoint(gitHubUserToken, USER_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.as(UserDTO.class), equalTo(expectedUser));
    }

    @Test
    public void queryUser_userGitLabUserExists_shouldSendCorrectUser() {
        // Given
        UserDTO expectedUser = new UserDTO(gitLabUser.getId(), gitLabUser.getUsername(),
                                           gitLabUser.getEmail(), gitLabUser.getPictureUrl());

        // When
        Response response = callGetRestEndpoint(gitLabUserToken, USER_ENDPOINT);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.as(UserDTO.class), equalTo(expectedUser));
    }

}
