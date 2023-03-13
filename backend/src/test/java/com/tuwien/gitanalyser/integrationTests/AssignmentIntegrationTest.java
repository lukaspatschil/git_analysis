package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.User;
import io.restassured.response.Response;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import utils.Randoms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssignmentIntegrationTest extends BaseIntegrationTest {

    private static final String REPOSITORY_ENDPOINT = "/apiV1/repository";

    private static final String ASSIGNMENT_EXTENSION = "/assignment";

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndNoRepositoryExists_shouldCreateNewRepositoryWithOneAssignmentAndOneSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(key, assignedName);

        prepareGitLabAllowedToAccess(platformId);

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Repository createdRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(createdRepository, platformId, gitLabUser, 1);

        Assignment assignment = createdRepository.getAssignments().get(0);
        assertAssignment(assignment, createAssignmentDTO.getKey(), 1);

        SubAssignment subAssignment = assignment.getSubAssignments().get(0);
        assertSubAssignment(subAssignment, createAssignmentDTO.getAssignedName());
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExists_shouldAddAssignmentAndSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(key, assignedName);

        prepareGitLabAllowedToAccess(platformId);
        addRepository(gitLabUser, platformId);

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Repository createdRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(createdRepository, platformId, gitLabUser, 1);

        Assignment assignment = createdRepository.getAssignments().get(0);
        assertAssignment(assignment, createAssignmentDTO.getKey(), 1);

        SubAssignment subAssignment = assignment.getSubAssignments().get(0);
        assertSubAssignment(subAssignment, createAssignmentDTO.getAssignedName());
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExists_shouldAddSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(key, assignedName);

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        addAssignment(key, repository);

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Repository createdRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(createdRepository, platformId, gitLabUser, 1);

        Assignment assignment = createdRepository.getAssignments().get(0);
        assertAssignment(assignment, createAssignmentDTO.getKey(), 1);
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExists_shouldAddSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(key, assignedName);

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Repository repositoryResult = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(repositoryResult, platformId, gitLabUser, 1);

        Assignment assignmentResult = repositoryResult.getAssignments().get(0);
        assertAssignment(assignmentResult, createAssignmentDTO.getKey(), 2);

        assertThat(assignmentResult.getSubAssignments(), containsInAnyOrder(
            hasProperty("assignedName", equalTo(subAssignment.getAssignedName())),
            hasProperty("assignedName", equalTo(createAssignmentDTO.getAssignedName()))
        ));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentWithDifferentKeyExistsAndSubAssignmentExists_shouldAddNewAssignmentAndSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String dtoKey = Randoms.alpha();
        String dtoName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(dtoKey, dtoName);

        String existingKey = "existingAssignmentKey";

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(existingKey, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Repository repositoryResult = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(repositoryResult, platformId, gitLabUser, 2);

        assertThat(repositoryResult.getAssignments(), containsInAnyOrder(
            allOf(
                hasFeature("key", Assignment::getKey, equalTo(dtoKey)),
                hasFeature("assignedValue", Assignment::getSubAssignments, containsInAnyOrder(
                    hasFeature("assignedName", SubAssignment::getAssignedName, equalTo(dtoName))
                ))
            ),
            allOf(
                hasFeature("key", Assignment::getKey, equalTo(assignment.getKey())),
                hasFeature("assignedValue", Assignment::getSubAssignments, containsInAnyOrder(
                    hasFeature("assignedName", SubAssignment::getAssignedName, equalTo(subAssignment.getAssignedName()))
                ))
            )
        ));
    }

    private void assertRepository(Repository createdRepository, long platformId, User user, int numberOfAssignments) {
        assertThat(createdRepository, notNullValue());
        assertThat(createdRepository.getUser().getId(), equalTo(user.getId()));
        assertThat(createdRepository.getPlatformId(), equalTo(platformId));
        assertThat(createdRepository.getAssignments().size(), equalTo(numberOfAssignments));
    }

    private void assertAssignment(Assignment assignment, String key, int numberOfSubAssignments) {
        assertThat(assignment.getKey(), equalTo(key));
        assertThat(assignment.getSubAssignments().size(), equalTo(numberOfSubAssignments));
    }

    private void assertSubAssignment(SubAssignment subAssignment, String assignedName) {
        assertThat(subAssignment.getAssignedName(), equalTo(assignedName));
    }

    private void prepareGitLabAllowedToAccess(long platformId) throws GitLabApiException {
        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = this.gitLabMockProjectApi(gitLabApi);
        Project project = mockProject(platformId);
        gitLabMockGetProject(projectApi, project);
    }

    private Project mockProject(long platformId) {
        var project = mock(Project.class);
        when(project.getId()).thenReturn(platformId);
        return project;
    }

    private Repository addRepository(User user, long platformId) {
        Repository repository = Repository.builder().user(user).platformId(platformId).build();
        repositoryRepository.save(repository);
        return repository;
    }

    private Assignment addAssignment(String key, Repository repository) {
        Assignment assignment = Assignment.builder().repository(repository).key(key).build();
        assignmentRepository.save(assignment);
        return assignment;
    }

    private SubAssignment addSubAssignment(Assignment assignment) {
        SubAssignment subAssignment =
            SubAssignment.builder().assignment(assignment).assignedName("AlreadyExistingSubAssignment").build();
        subAssignmentRepository.save(subAssignment);
        return subAssignment;
    }

    @Test
    public void addAssignment_userNotAllowedAndNoRepositoryExists_shouldReturn403()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        var createAssignmentDTO = new CreateAssignmentDTO(key, assignedName);

        GitLabApi gitLabApi = gitLabMockFactory();
        ProjectApi projectApi = this.gitLabMockProjectApi(gitLabApi);
        gitLabMockGetProjectThrowsGitLabException(projectApi, platformId);

        // When
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN.value()));
    }
}
