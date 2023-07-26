package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.repository.AssignmentRepository;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.repository.SubAssignmentRepository;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.security.jwt.FingerprintService;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.service.apiCalls.factory.GitHubAPIFactory;
import com.tuwien.gitanalyser.service.apiCalls.factory.GitLabAPIFactory;
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

    protected static final String GITHUB_LOGIN_ENDPOINT = "/oauth2/authorization/github";
    protected static final String GITLAB_LOGIN_ENDPOINT = "/oauth2/authorization/gitlab";
    protected static final String REPOSITORY_ENDPOINT = "/apiV1/repository";
    protected static final String REFRESH_ENDPOINT = "/apiV1/refresh";
    protected static final String USER_ENDPOINT = "/apiV1/user";
    protected static final String ASSIGNMENT_EXTENSION = "/assignment";
    protected static final String BRANCHES_ENDPOINT_EXTENSION = "branch";
    protected static final String COMMITTER_ENDPOINT_EXTENSION = "committer";
    protected static final String STATS_ENDPOINT_EXTENSION = "stats";
    protected static final String COMMITS_ENDPOINT_EXTENSION = "commit";
    private static final String SERVER_HOST = "http://localhost";
    protected String gitHubUserToken;
    protected String gitHubAccessToken;

    protected String gitLabUserToken;
    protected String gitLabAccessToken;
    protected String gitLabFingerprint;
    protected String gitHubFingerprint;
    protected String gitHubRefreshToken;
    protected String gitLabRefreshToken;

    @Autowired
    protected GitHubAPIFactory gitHubAPIFactory;
    @Autowired
    protected GitLabAPIFactory gitLabAPIFactory;
    protected User gitLabUser;
    protected User gitHubUser;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RepositoryRepository repositoryRepository;
    @Autowired
    protected AssignmentRepository assignmentRepository;
    @Autowired
    protected SubAssignmentRepository subAssignmentRepository;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    @Autowired
    private FingerprintService fingerPrintService;
    @LocalServerPort
    private int port;

    @Before
    public void beforeBase() {

        RestAssured.baseURI = SERVER_HOST;
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        gitHubAccessToken = "JohnsRandomAccessToken";
        gitLabAccessToken = "TomsRandomAccessToken";

        gitHubFingerprint = "gitHub";
        gitLabFingerprint = "gitLab";

        gitHubUser = createUser("John", "john@random.com", gitHubAccessToken, Randoms.integer(),
                                AuthenticationProvider.GITHUB, "https://github.com/pictureURL", gitHubFingerprint);
        gitHubUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createAccessToken(gitHubUser.getId())).with("");
        gitHubRefreshToken = jwtTokenProvider.createRefreshToken(gitHubUser.getId());

        gitLabUser = createUser("Tom", "tom@random.com", gitLabAccessToken, Randoms.integer(),
                                AuthenticationProvider.GITLAB, "https://gitlab.com/pictureURL", gitLabFingerprint);
        gitLabUserToken = Strings.join(AuthenticationConstants.TOKEN_PREFIX,
                                       jwtTokenProvider.createAccessToken(gitLabUser.getId())).with("");
        gitLabRefreshToken = jwtTokenProvider.createRefreshToken(gitLabUser.getId());
    }

    @After
    public void afterBase() {
        subAssignmentRepository.deleteAll();
        assignmentRepository.deleteAll();
        repositoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String username, String email, String accessToken, Integer platformId,
                            AuthenticationProvider authenticationProvider, String pictureUrl, String fingerprint) {
        User user = User.builder()
                        .username(username)
                        .email(email)
                        .accessToken(accessToken)
                        .platformId(platformId)
                        .authenticationProvider(authenticationProvider)
                        .fingerPrintHash(fingerPrintService.sha256(fingerprint))
                        .pictureUrl(pictureUrl)
                        .build();

        if (user.getId() == null) {
            user = userRepository.save(user);
        }
        return user;
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

    protected GHRepository gitHubMockGHRepository(GitHub gitHubApi, Long platformId) throws IOException {
        GHRepository ghRepository = mock(GHRepository.class);
        when(gitHubApi.getRepositoryById(platformId)).thenReturn(ghRepository);
        return ghRepository;
    }

    protected void gitLabMockMemberProjects(ProjectApi projectApi, List<Project> projects) throws GitLabApiException {
        when(projectApi.getMemberProjects()).thenReturn(projects);
    }

    protected void gitLabMockGetProject(ProjectApi projectApi, Project project) throws GitLabApiException {
        when(projectApi.getProject(project.getId())).thenReturn(project);
    }

    protected void gitLabMockGetProjectThrowsGitLabException(ProjectApi projectApi, Long projectId)
        throws GitLabApiException {
        when(projectApi.getProject(projectId)).thenThrow(new GitLabApiException("not Found"));
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
                                   false,
                                   true,
                                   null)).thenReturn(List.of(commits));
    }

    protected Project gitLabCreateRandomProject() {
        Project ownedProject = new Project();
        ownedProject.setId(Randoms.getLong());
        ownedProject.setHttpUrlToRepo(Randoms.alpha());
        ownedProject.setName(Randoms.alpha());
        return ownedProject;
    }

    protected Repository addRepository(User user, long platformId) {
        Repository repository = Repository.builder().user(user).platformId(platformId).build();
        repositoryRepository.save(repository);
        return repository;
    }

    protected Assignment addAssignment(String key, Repository repository) {
        Assignment assignment = Assignment.builder().repository(repository).key(key).build();
        assignmentRepository.save(assignment);
        return assignment;
    }

    protected SubAssignment addSubAssignment(Assignment assignment) {
        SubAssignment subAssignment =
            SubAssignment.builder().assignment(assignment).assignedName(Randoms.alpha()).build();
        subAssignmentRepository.save(subAssignment);
        return subAssignment;
    }

    protected Response callGetRestEndpoint(String authorizationToken, String url) {
        return RestAssured.given().log().all()
                          .contentType(ContentType.JSON)
                          .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                          .when().get(url)
                          .then().extract().response();
    }

    protected Response callGetRestEndpoint(String authorizationToken, String url, Map<String, String> queryParams) {
        return RestAssured.given().log().all()
                          .contentType(ContentType.JSON)
                          .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                          .when().queryParams(queryParams).get(url)
                          .then().extract().response();
    }

    protected Response callDeleteRestEndpoint(String authorizationToken, String url) {
        return RestAssured.given().log().all()
                          .contentType(ContentType.JSON)
                          .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                          .when().delete(url)
                          .then().extract().response();
    }

    protected Response callPostRestEndpoint(String authorizationToken, String url, Object body) {
        return RestAssured.given().log().all()
                          .contentType(ContentType.JSON)
                          .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                          .body(body)
                          .when().post(url)
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
