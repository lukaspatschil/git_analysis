package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPIFactory;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPIFactory;
import io.restassured.RestAssured;
import org.assertj.core.util.Strings;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import utils.Randoms;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BaseIntegrationTest.IntegrationTestDependencyInjection.class)
public abstract class BaseIntegrationTest {

    private static final String SERVER_HOST = "http://localhost";

    protected String gitHubUserToken;
    protected String gitHubAccessToken;
    protected GitHub githubObject;


    protected String gitLabUserToken;
    protected String gitLabAccessToken;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    @LocalServerPort
    private int port;
    @Autowired
    protected GitHubAPIFactory gitHubAPIFactory;

    @Autowired
    protected GitLabAPIFactory gitLabAPIFactory;

    @Before
    public void beforeBase() {

        RestAssured.baseURI = SERVER_HOST;
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        gitHubAccessToken = "JohnsRandomAccessToken";
        gitLabAccessToken = "TomsRandomAccessToken";

        User gitHubUser = createUser("John", "john@random.com", gitHubAccessToken, Randoms.integer(),
                                     AuthenticationProvider.GITHUB);
        gitHubUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createToken(gitHubUser.getId()))
                                 .with(" ");

        User gitLabUser = createUser("Tom", "tom@random.com", gitLabAccessToken, Randoms.integer(),
                                     AuthenticationProvider.GITLAB);
        gitLabUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createToken(gitLabUser.getId()))
                                 .with(" ");

    }

    private User createUser(String username, String email, String accessToken, Integer platformId,
                            AuthenticationProvider authenticationProvider) {
        User gitLabUser = User.builder()
                              .username(username)
                              .email(email)
                              .accessToken(accessToken)
                              .platformId(platformId)
                              .authenticationProvider(authenticationProvider)
                              .build();

        if (gitLabUser.getId() == null) {
            userRepository.save(gitLabUser);
        }
        return gitLabUser;
    }

    @After
    public void afterBase() {
        userRepository.deleteAll();
    }

    protected void gitHubMockAPI() throws IOException {
        githubObject = mock(GitHub.class);
        GHMyself ghMyself = mock(GHMyself.class);

        when(gitHubAPIFactory.createObject(gitHubAccessToken)).thenReturn(githubObject);
        when(githubObject.getMyself()).thenReturn(ghMyself);
    }

    protected GitLabApi gitLabMockApi() {
        GitLabApi gitLabApi = mock(GitLabApi.class);
        when(gitLabAPIFactory.createObject(gitLabAccessToken)).thenReturn(gitLabApi);
        return gitLabApi;
    }

    protected ProjectApi gitLabMockProjectApi() {
        GitLabApi gitLabApi = gitLabMockApi();
        ProjectApi projectApi = mock(ProjectApi.class);
        when(gitLabApi.getProjectApi()).thenReturn(projectApi);
        return projectApi;
    }

    protected void gitLabMockMemberProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getMemberProjects()).thenReturn(projects);
    }

    protected void gitLabMockOwnedProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getOwnedProjects()).thenReturn(projects);
    }

    protected static Project gitLabCreateRandomProject() {
        Project ownedProject = new Project();
        ownedProject.setId(Randoms.getLong());
        ownedProject.setHttpUrlToRepo(Randoms.alpha());
        ownedProject.setName(Randoms.alpha());
        return ownedProject;
    }

    @TestConfiguration
    static class IntegrationTestDependencyInjection {
        @Bean
        public GitHubAPIFactory gitHubAPIFactory() {
            return mock(GitHubAPIFactory.class);
        }

        @Bean
        public GitLabAPIFactory gitLabAPIFactory() {
            return mock(GitLabAPIFactory.class);
        }

    }
}
