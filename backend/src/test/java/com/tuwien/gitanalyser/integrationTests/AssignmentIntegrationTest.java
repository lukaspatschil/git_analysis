package com.tuwien.gitanalyser.integrationTests;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.Matchers.assignmentMatcher;
import static utils.Matchers.subAssignmentDTOMatcher;
import static utils.Matchers.subAssignmentMatcher;

public class AssignmentIntegrationTest extends BaseIntegrationTest {

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
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));

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
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));

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
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));

        Repository createdRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId).get();
        assertRepository(createdRepository, platformId, gitLabUser, 1);

        Assignment assignment = createdRepository.getAssignments().get(0);
        assertAssignment(assignment, createAssignmentDTO.getKey(), 1);
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndDifferentName_shouldAddSubAssignment()
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
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));

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
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));
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

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndSameName_shouldReturn201()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        var createAssignmentDTO = new CreateAssignmentDTO(key, subAssignment.getAssignedName());

        // When
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndKeyEqualToAssignedName_shouldThrowConflictException()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String randomName = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);

        var createAssignmentDTO = new CreateAssignmentDTO(randomName, randomName);

        // When
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndSameName_deleteSubAssignmentAndCreateNewSubAssignmentForCorrectKey()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String newKey = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        var createAssignmentDTO = new CreateAssignmentDTO(newKey, subAssignment.getAssignedName());

        // When
        Response response = callPostRestEndpoint(gitLabUserToken,
                                                 REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                                                 createAssignmentDTO);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED.value()));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndSameName_shouldRemoveOldSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String newKey = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        var createAssignmentDTO = new CreateAssignmentDTO(newKey, subAssignment.getAssignedName());

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Optional<Repository> resultRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId);
        assertThat(resultRepository, isPresent());
        assertThat(resultRepository.get().getAssignments().size(), equalTo(1));
        assertThat(resultRepository.get().getAssignments(), not(hasItem(hasFeature("oldKey", Assignment::getKey, equalTo(key)))));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndSameName_shouldCreateNewAssignmentInDatabase()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String newKey = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        var createAssignmentDTO = new CreateAssignmentDTO(newKey, subAssignment.getAssignedName());

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Optional<Repository> resultRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId);
        assertThat(resultRepository, isPresent());
        assertThat(resultRepository.get().getAssignments().size(), equalTo(1));
        assertThat(resultRepository.get().getAssignments(), not(hasItem(hasFeature("oldKey", Assignment::getKey, equalTo(key)))));
    }

    @Test
    public void addAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExistsAndSameName_shouldCreateNewAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String newKey = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        var createAssignmentDTO = new CreateAssignmentDTO(newKey, subAssignment.getAssignedName());

        // When
        callPostRestEndpoint(gitLabUserToken,
                             REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION,
                             createAssignmentDTO);

        // Then
        Optional<Repository> resultRepository = repositoryRepository.findByUserAndPlatformId(gitLabUser, platformId);
        assertThat(resultRepository, isPresent());
        assertThat(resultRepository.get().getAssignments().size(), equalTo(1));
        assertThat(resultRepository.get().getAssignments(), hasItem(
            allOf(
                hasFeature("key", Assignment::getKey, equalTo(newKey)),
                allOf(
                    hasFeature("subAssignments", Assignment::getSubAssignments, hasSize(1)),
                    hasFeature("subAssignments", Assignment::getSubAssignments, hasItem(
                        allOf(
                            hasFeature("assignedName", SubAssignment::getAssignedName, equalTo(subAssignment.getAssignedName()))
                        )
                    ))
                )
            )
        ));
    }

    @Test
    public void getAssignments_userNotAllowedToAccessRepository_returnsForbidden() {
        // Given
        long platformId = Randoms.getLong();

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void getAssignments_userAllowedToAccessRepositoryAndDoesNotExist_returnsNotFound()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();

        prepareGitLabAllowedToAccess(platformId);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void getAssignments_userAllowedToAccessRepositoryExists_returnsEmptyList()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();

        prepareGitLabAllowedToAccess(platformId);
        addRepository(gitLabUser, platformId);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        assertThat(response.as(AssignmentDTO[].class).length, equalTo(0));
    }

    @Test
    public void getAssignments_userAllowedToAccessRepositoryExistsAndAssignmentExists_returnsEmptyAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        addAssignment(key, repository);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        List<AssignmentDTO> assignments = Arrays.asList(response.as(AssignmentDTO[].class));
        assertThat(assignments.size(), equalTo(1));
        assertThat(assignments, containsInAnyOrder(
            allOf(
                hasFeature("key", AssignmentDTO::getKey, equalTo(key))
            )
        ));
    }

    @Test
    public void getAssignments_userAllowedToAccessRepositoryExistsAndAssignmentExistsAndSubAssignmentExists_returnsCorrectAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);
        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        List<AssignmentDTO> assignments = Arrays.asList(response.as(AssignmentDTO[].class));
        assertThat(assignments.size(), equalTo(1));

        assertThat(assignments, containsInAnyOrder(
            allOf(
                hasFeature("key", AssignmentDTO::getKey, equalTo(key)),
                hasFeature("subAssignment", AssignmentDTO::getAssignedNames, containsInAnyOrder(
                               subAssignmentDTOMatcher(subAssignment)
                           )
                )
            )
        ));
    }

    @Test
    public void getAssignments_userAllowedToAccessRepositoryExistsAndMultipleAssignmentsExistAndMultipleSubAssignmentExist_returnsCorrectAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key1 = Randoms.alpha();
        String key2 = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);

        Repository repository1 = addRepository(gitLabUser, platformId);
        Assignment assignment1 = addAssignment(key1, repository1);
        SubAssignment subAssignment11 = addSubAssignment(assignment1);
        SubAssignment subAssignment12 = addSubAssignment(assignment1);

        Assignment assignment2 = addAssignment(key2, repository1);
        SubAssignment subAssignment21 = addSubAssignment(assignment2);
        SubAssignment subAssignment22 = addSubAssignment(assignment2);

        // When
        Response response = callGetRestEndpoint(gitLabUserToken,
                                                REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION);

        // Then
        List<AssignmentDTO> assignments = Arrays.asList(response.as(AssignmentDTO[].class));
        assertThat(assignments.size(), equalTo(2));

        assertThat(assignments, containsInAnyOrder(
            assignmentMatcher(key1, subAssignment11, subAssignment12),
            assignmentMatcher(key2, subAssignment21, subAssignment22)
        ));
    }

    @Test
    public void deleteAssignment_userNotAllowedToAccessRepository_returnsForbidden() {
        // Given
        long platformId = Randoms.getLong();
        long subAssignmentId = Randoms.getLong();

        // When
        Response response = callDeleteRestEndpoint(gitLabUserToken,
                                                   REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                                       "/" + subAssignmentId);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void deleteAssignment_userAllowedToAccessRepositoryAndRepositoryDoesNotExist_returnsNotFound()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        long subAssignmentId = Randoms.getLong();

        prepareGitLabAllowedToAccess(platformId);

        // When
        Response response = callDeleteRestEndpoint(gitLabUserToken,
                                                   REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                                       "/" + subAssignmentId);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void deleteAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndNoAssignmentExists_returnsNotFound()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        long subAssignmentId = Randoms.getLong();

        prepareGitLabAllowedToAccess(platformId);

        addRepository(gitLabUser, platformId);

        // When
        Response response = callDeleteRestEndpoint(gitLabUserToken,
                                                   REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                                       "/" + subAssignmentId);

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void deleteAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExists_returnsOk()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);

        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        // When
        Response response = callDeleteRestEndpoint(gitLabUserToken,
                                                   REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                                       "/" + subAssignment.getId());

        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK.value()));
    }

    @Test
    public void deleteAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndSubAssignmentExists_deletesAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);

        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment = addSubAssignment(assignment);

        // When
        callDeleteRestEndpoint(gitLabUserToken,
                               REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                   "/" + subAssignment.getId());

        // Then
        Optional<Assignment> result = assignmentRepository.findById(assignment.getId());
        assertThat(result, isEmpty());
    }

    @Test
    public void deleteAssignment_userAllowedToAccessRepositoryAndRepositoryExistsAndAssignmentExistsAndMultipleSubAssignmentsExist_deletesSubAssignment()
        throws GitLabApiException {
        // Given
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();

        prepareGitLabAllowedToAccess(platformId);

        Repository repository = addRepository(gitLabUser, platformId);
        Assignment assignment = addAssignment(key, repository);
        SubAssignment subAssignment1 = addSubAssignment(assignment);
        SubAssignment subAssignment2 = addSubAssignment(assignment);

        // When
        callDeleteRestEndpoint(gitLabUserToken,
                               REPOSITORY_ENDPOINT + "/" + platformId + ASSIGNMENT_EXTENSION +
                                   "/" + subAssignment1.getId());

        // Then
        Optional<Assignment> result = assignmentRepository.findById(assignment.getId());
        assertThat(result, isPresent());
        assertThat(result.get().getSubAssignments().size(), equalTo(1));
        assertThat(result.get().getSubAssignments(), containsInAnyOrder(
            subAssignmentMatcher(subAssignment2)
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
}
