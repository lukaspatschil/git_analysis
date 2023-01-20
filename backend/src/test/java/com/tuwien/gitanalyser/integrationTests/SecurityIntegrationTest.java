package com.tuwien.gitanalyser.integrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    private static final String GITHUB_LOGIN_ENDPOINT = "/oauth2/authorization/github";
    private static final String GITLAB_LOGIN_ENDPOINT = "/oauth2/authorization/gitlab";

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";
    @Autowired
    private MockMvc mvc;

    @Test
    public void queryGitLabLoginURL_always_shouldRedirect() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GITLAB_LOGIN_ENDPOINT)
                                          .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void queryGitHubLoginURL_always_shouldRedirect() throws Exception {
        mvc.perform(
            MockMvcRequestBuilders.get(GITHUB_LOGIN_ENDPOINT)
                                  .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void queryAllRepositories_withoutAuthentication_shouldSend401() throws Exception {
        mvc.perform(
            MockMvcRequestBuilders.get(REPOSITORY_ENDPOINT)
                                  .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }
}
