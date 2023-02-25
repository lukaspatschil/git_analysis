package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.service.APICalls.factory.GitHubAPIFactory;
import com.tuwien.gitanalyser.service.APICalls.factory.GitLabAPIFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.util.Strings;
import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import utils.Randoms;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BaseIntegrationTest.IntegrationTestDependencyInjection.class)
public abstract class BaseIntegrationTest {

    private static final String SERVER_HOST = "http://localhost";

    protected String gitHubUserToken;
    protected String gitHubAccessToken;

    protected String gitLabUserToken;
    protected String gitLabAccessToken;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected GitHubAPIFactory gitHubAPIFactory;
    @Autowired
    protected GitLabAPIFactory gitLabAPIFactory;
    protected User gitLabUser;
    protected User gitHubUser;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    @LocalServerPort
    private int port;

    protected static Project gitLabCreateRandomProject() {
        Project ownedProject = new Project();
        ownedProject.setId(Randoms.getLong());
        ownedProject.setHttpUrlToRepo(Randoms.alpha());
        ownedProject.setName(Randoms.alpha());
        return ownedProject;
    }

    @Before
    public void beforeBase() {

        RestAssured.baseURI = SERVER_HOST;
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        gitHubAccessToken = "JohnsRandomAccessToken";
        gitLabAccessToken = "TomsRandomAccessToken";

        gitHubUser = createUser("John", "john@random.com", gitHubAccessToken, Randoms.integer(),
                                AuthenticationProvider.GITHUB, "https://github.com/pictureURL");
        gitHubUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createToken(gitHubUser.getId()))
                                 .with(" ");

        gitLabUser = createUser("Tom", "tom@random.com", gitLabAccessToken, Randoms.integer(),
                                AuthenticationProvider.GITLAB, "https://gitlab.com/pictureURL");
        gitLabUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createToken(gitLabUser.getId()))
                                 .with(" ");

    }

    @After
    public void afterBase() {
        userRepository.deleteAll();
    }

    private User createUser(String username, String email, String accessToken, Integer platformId,
                            AuthenticationProvider authenticationProvider, String pictureUrl) {
        User gitLabUser = User.builder()
                              .username(username)
                              .email(email)
                              .accessToken(accessToken)
                              .platformId(platformId)
                              .authenticationProvider(authenticationProvider)
                              .pictureUrl(pictureUrl)
                              .build();

        if (gitLabUser.getId() == null) {
            gitLabUser = userRepository.save(gitLabUser);
        }
        return gitLabUser;
    }

    protected GHMyself gitHubMockAPI() throws IOException {
        GitHub githubObject = gitHubMockFactory();
        GHMyself ghMyself = mock(GHMyself.class);
        when(githubObject.getMyself()).thenReturn(ghMyself);
        return ghMyself;
    }

    protected GitHub gitHubMockFactory() throws IOException {
        var githubObject = mock(GitHub.class);
        when(gitHubAPIFactory.createObject(gitHubAccessToken)).thenReturn(githubObject);
        return githubObject;
    }

    protected GitLabApi gitLabMockFactory() {
        GitLabApi gitLabApi = mock(GitLabApi.class);
        when(gitLabAPIFactory.createObject(gitLabAccessToken)).thenReturn(gitLabApi);
        return gitLabApi;
    }

    protected RepositoryApi gitLabMockRepositoryApi(GitLabApi gitLabApi) {
        RepositoryApi repositoryApi = mock(RepositoryApi.class);
        when(gitLabApi.getRepositoryApi()).thenReturn(repositoryApi);
        return repositoryApi;
    }

    protected ProjectApi gitLabMockProjectApi(GitLabApi gitLabApi) {
        ProjectApi projectApi = mock(ProjectApi.class);
        when(gitLabApi.getProjectApi()).thenReturn(projectApi);
        return projectApi;
    }

    protected CommitsApi gitLabMockCommitsApi(GitLabApi gitLabApi) {
        CommitsApi commitsApi = mock(CommitsApi.class);
        when(gitLabApi.getCommitsApi()).thenReturn(commitsApi);
        return commitsApi;
    }

    protected void gitHubMockBranches(GHRepository ghRepository) throws IOException {
        when(ghRepository.getBranches()).thenReturn(Map.of());
    }

    protected GHRepository gitHubMockGHRepository(Long repositoryId, GitHub gitLabApi) throws IOException {
        GHRepository ghRepository = mock(GHRepository.class);
        when(gitLabApi.getRepositoryById(repositoryId)).thenReturn(ghRepository);
        return ghRepository;
    }

    protected void gitLabMockMemberProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getMemberProjects()).thenReturn(projects);
    }

    protected void gitLabMockGetProject(ProjectApi projectApi, Project projects) throws GitLabApiException {
        when(projectApi.getProject(projects.getId())).thenReturn(projects);
    }

    protected void gitLabMockOwnedProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getOwnedProjects()).thenReturn(projects);
    }

    protected void gitLabMockGetCommits(CommitsApi commitsApi, Long repositoryId, String branch, Commit... commits)
        throws GitLabApiException {
        when(commitsApi.getCommits(repositoryId,
                                   branch,
                                   null,
                                   null,
                                   null,
                                   true,
                                   true,
                                   null)).thenReturn(List.of(commits));
    }

    protected Response callRestEndpoint(String authorizationToken, String url) {
        return RestAssured.given().log().all()
                          .contentType(ContentType.JSON)
                          .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                          .when().get(url)
                          .then().extract().response();
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
