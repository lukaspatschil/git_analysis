package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.mapper.AssignmentMapper;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitterMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.exception.BadRequestException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitHubException;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.InternalServerErrorException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.Authentication;
import utils.Randoms;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryEndpointTest {

    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_1 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_2 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_3 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_1 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getUrl());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_2 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getUrl());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_3 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getUrl());

    private static final BranchInternalDTO BRANCH_INTERNAL_DTO_1 =
        new BranchInternalDTO(Randoms.alpha());
    private static final BranchInternalDTO BRANCH_INTERNAL_DTO_2 =
        new BranchInternalDTO(Randoms.alpha());

    private static final BranchDTO BRANCH_DTO_1 =
        new BranchDTO(BRANCH_INTERNAL_DTO_1.getName());
    private static final BranchDTO BRANCH_DTO_2 =
        new BranchDTO(BRANCH_INTERNAL_DTO_2.getName());
    private static final CommitterInternalDTO COMMITTER_INTERNAL_DTO_1 = new CommitterInternalDTO(Randoms.alpha());
    private static final CommitterDTO COMMITTER_DTO_1 = new CommitterDTO(COMMITTER_INTERNAL_DTO_1.getName());

    private static final CommitterInternalDTO COMMITTER_INTERNAL_DTO_2 = new CommitterInternalDTO(Randoms.alpha());
    private static final CommitterDTO COMMITTER_DTO_2 = new CommitterDTO(COMMITTER_INTERNAL_DTO_2.getName());

    private final String defaultBranch = "develop";
    private final CommitDTO commitDTO1 = mock(CommitDTO.class);
    private final CommitDTO commitDTO2 = mock(CommitDTO.class);
    private final CommitInternalDTO commit1 = mock(CommitInternalDTO.class);
    private final CommitInternalDTO commit2 = mock(CommitInternalDTO.class);
    private RepositoryService repositoryService;
    private GitService gitService;
    private NotSavedRepositoryMapper notSavedRepositoryMapper;
    private RepositoryEndpoint sut;
    private BranchMapper branchMapper;
    private CommitMapper commitMapper;
    private CommitterMapper committerMapper;
    private AssignmentMapper assignmentMapper;

    @BeforeEach
    void setUp() {
        repositoryService = mock(RepositoryService.class);
        gitService = mock(GitService.class);
        notSavedRepositoryMapper = mock(NotSavedRepositoryMapper.class);
        branchMapper = mock(BranchMapper.class);
        commitMapper = mock(CommitMapper.class);
        committerMapper = mock(CommitterMapper.class);
        assignmentMapper = mock(AssignmentMapper.class);
        sut = new RepositoryEndpoint(repositoryService,
                                     gitService,
                                     notSavedRepositoryMapper,
                                     branchMapper,
                                     commitMapper,
                                     committerMapper,
                                     assignmentMapper);
    }

    @Test
    void getAllRepositories_always_shouldCallService()
        throws InternalServerErrorException, BadRequestException, GitException, NoProviderFoundException {
        // Given
        Authentication authentication = mock(Authentication.class);

        long userId = Randoms.getLong();
        mockUserId(userId, authentication);

        // When
        sut.getAllRepositories(authentication);

        // Then
        verify(gitService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_serviceReturnsListOfRepositories_returnList()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllRepositories(userId, List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3));

        prepareNotSavedRepositoryMapper(List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3), List.of(
            NOT_SAVED_REPOSITORY_DTO_1,
            NOT_SAVED_REPOSITORY_DTO_2,
            NOT_SAVED_REPOSITORY_DTO_3));

        // When
        List<NotSavedRepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, containsInAnyOrder(NOT_SAVED_REPOSITORY_DTO_1,
                                                      NOT_SAVED_REPOSITORY_DTO_2,
                                                      NOT_SAVED_REPOSITORY_DTO_3));
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyListOfRepositories_returnList()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllRepositories(userId, List.of());

        // When
        List<NotSavedRepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, equalTo(List.of()));
    }

    @ParameterizedTest
    @ValueSource(classes = {GitLabException.class, GitHubException.class})
    void getAllRepositories_serviceThrowsGitException_throwsBadRequestException(Class<? extends GitException> exception)
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllRepositoriesThrows(userId, exception);

        // When + Then
        assertThrows(BadRequestException.class, () -> sut.getAllRepositories(authentication));
    }

    @Test
    void getAllRepositories_serviceThrowsNoProviderFound_throwsInternalServerErrorException()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllRepositoriesThrows(userId, NoProviderFoundException.class);

        // When + Then
        assertThrows(InternalServerErrorException.class, () -> sut.getAllRepositories(authentication));
    }

    @Test
    void getRepositoryById_always_shouldCallService()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(Randoms.getLong(), authentication);

        // When
        sut.getRepositoryById(authentication, platformId);

        // Then
        verify(gitService).getRepositoryById(Long.parseLong(authentication.getName()), platformId);
    }

    @Test
    void getRepositoryById_givenOneRepository_returnsRepository()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetRepositoryById(platformId, userId, NOT_SAVED_REPOSITORY_INTERNAL_DTO_1);
        prepareNotSavedRepositoryMapper(NOT_SAVED_REPOSITORY_INTERNAL_DTO_1, NOT_SAVED_REPOSITORY_DTO_1);

        // When
        var result = sut.getRepositoryById(authentication, platformId);

        // Then
        assertThat(result, equalTo(NOT_SAVED_REPOSITORY_DTO_1));
    }

    @ParameterizedTest
    @ValueSource(classes = {GitLabException.class, GitHubException.class})
    void getRepositoryById_serviceThrowsGitException_throwsBadRequestException(Class<? extends GitException> exception)
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetRepositoryByIdThrows(platformId, userId, exception);

        // When + Then
        assertThrows(BadRequestException.class, () -> sut.getRepositoryById(authentication, platformId));
    }

    @Test
    void getRepositoryById_serviceThrowsNoProviderFound_throwsInternalServerErrorException()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllRepositories(userId, List.of());
        prepareGetRepositoryByIdThrows(platformId, userId, NoProviderFoundException.class);

        // When + Then
        assertThrows(InternalServerErrorException.class, () -> sut.getRepositoryById(authentication, platformId));
    }

    @Test
    void getBranchesByRepositoryId_always_shouldCallService()
        throws GitException, InternalServerErrorException, BadRequestException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getBranchesByRepositoryId(authentication, platformId);

        // Then
        verify(gitService).getAllBranches(userId, platformId);
    }

    @Test
    void getBranchesByRepositoryId_givenOneBranch_returnsListWithOneItem()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllBranches(platformId, userId, List.of(BRANCH_INTERNAL_DTO_1));
        prepareBranchMapper(List.of(BRANCH_INTERNAL_DTO_1), List.of(BRANCH_DTO_1));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, platformId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1));
    }

    @Test
    void getBranchesByRepositoryId_givenTwoBranches_returnsListWithTwoItems()
        throws GitException, NoProviderFoundException, InternalServerErrorException, BadRequestException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllBranches(platformId, userId, List.of(
            BRANCH_INTERNAL_DTO_1, BRANCH_INTERNAL_DTO_2));
        prepareBranchMapper(List.of(BRANCH_INTERNAL_DTO_1, BRANCH_INTERNAL_DTO_2),
                            List.of(BRANCH_DTO_1, BRANCH_DTO_2));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, platformId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1, BRANCH_DTO_2));
    }

    @ParameterizedTest
    @ValueSource(classes = {GitLabException.class, GitHubException.class})
    void getBranchesByRepositoryId_serviceThrowsGitException_throwsBadRequestException(
        Class<? extends GitException> exception) throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllBranchesThrows(platformId, userId, exception);

        // When + Then
        assertThrows(BadRequestException.class, () -> sut.getBranchesByRepositoryId(authentication, platformId));
    }

    @Test
    void getBranchesByRepositoryId_serviceThrowsNoProviderFound_throwsInternalServerErrorException()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllBranchesThrows(platformId, userId, NoProviderFoundException.class);

        // When + Then
        assertThrows(InternalServerErrorException.class,
                     () -> sut.getBranchesByRepositoryId(authentication, platformId));
    }

    @Test
    void getCommitsByRepositoryId_always_shouldCallService()
        throws GitException, BadRequestException, InternalServerErrorException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getCommitsByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        verify(gitService).getAllCommits(userId, platformId, defaultBranch);
    }

    @Test
    void getCommitsByRepositoryId_givenOneCommit_returnsListWithOneCommit()
        throws GitException, NoProviderFoundException, BadRequestException, InternalServerErrorException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        CommitInternalDTO commit = mock(CommitInternalDTO.class);
        CommitDTO commitDTO = mock(CommitDTO.class);
        prepareGetAllCommits(platformId, userId, defaultBranch, List.of(commit));
        prepareCommitsMapper(List.of(commit), List.of(commitDTO));

        // When
        List<CommitDTO> commits = sut.getCommitsByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        assertThat(commits, containsInAnyOrder(commitDTO));
    }

    @Test
    void getCommitsByRepositoryId_givenTwoCommits_returnsListWithTwoCommits()
        throws GitException, NoProviderFoundException, BadRequestException, InternalServerErrorException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommits(platformId, userId, defaultBranch, List.of(commit1, commit2));
        prepareCommitsMapper(List.of(commit1, commit2), List.of(commitDTO1, commitDTO2));

        // When
        List<CommitDTO> branches = sut.getCommitsByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        assertThat(branches, equalTo(List.of(commitDTO1, commitDTO2)));
    }

    @ParameterizedTest
    @ValueSource(classes = {GitLabException.class, GitHubException.class})
    void getCommitsByRepositoryId_serviceThrowsGitException_throwsBadRequestException(Class<? extends GitException> exception)
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommitsThrows(platformId, userId, defaultBranch, exception);

        // When + Then
        assertThrows(BadRequestException.class,
                     () -> sut.getCommitsByRepositoryId(authentication, platformId, defaultBranch));
    }

    @Test
    void getCommitsByRepositoryId_serviceThrowsNoProviderFound_throwsInternalServerErrorException()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommitsThrows(platformId, userId, defaultBranch, NoProviderFoundException.class);

        // When + Then
        assertThrows(InternalServerErrorException.class,
                     () -> sut.getCommitsByRepositoryId(authentication, platformId, defaultBranch));
    }

    @Test
    void getCommittersByRepositoryId_always_shouldCallService()
        throws GitException, InternalServerErrorException, BadRequestException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getCommittersByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        verify(gitService).getAllCommitters(userId, platformId, defaultBranch);
    }

    @Test
    void getCommittersByRepositoryId_givenOneCommitter_returnsListWithOneItem()
        throws GitException, InternalServerErrorException, BadRequestException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        prepareGetAllCommitters(platformId, userId, defaultBranch, Set.of(COMMITTER_INTERNAL_DTO_1));
        prepareCommitterMapper(Set.of(COMMITTER_INTERNAL_DTO_1), List.of(COMMITTER_DTO_1));

        // When
        List<CommitterDTO> result = sut.getCommittersByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(COMMITTER_DTO_1));
    }

    @Test
    void getCommittersByRepositoryId_givenTwoCommitters_returnsListWithTwoItems()
        throws GitException, InternalServerErrorException, BadRequestException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommitters(platformId,
                                userId,
                                defaultBranch,
                                Set.of(COMMITTER_INTERNAL_DTO_1, COMMITTER_INTERNAL_DTO_2));
        prepareCommitterMapper(Set.of(COMMITTER_INTERNAL_DTO_1, COMMITTER_INTERNAL_DTO_2),
                               List.of(COMMITTER_DTO_1, COMMITTER_DTO_2));

        // When
        List<CommitterDTO> result = sut.getCommittersByRepositoryId(authentication, platformId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(COMMITTER_DTO_1, COMMITTER_DTO_2));
    }

    @ParameterizedTest
    @ValueSource(classes = {GitLabException.class, GitHubException.class})
    void getCommittersByRepositoryId_serviceThrowsGitException_throwsBadRequestException(Class<? extends GitException> exception)
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommittersThrows(platformId, userId, defaultBranch, exception);

        // When + Then
        assertThrows(BadRequestException.class,
                     () -> sut.getCommittersByRepositoryId(authentication, platformId, defaultBranch));
    }

    @Test
    void getCommittersByRepositoryId_serviceThrowsNoProviderFound_throwsInternalServerErrorException()
        throws GitException, NoProviderFoundException {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        prepareGetAllCommittersThrows(platformId, userId, defaultBranch, NoProviderFoundException.class);

        // When + Then
        assertThrows(InternalServerErrorException.class,
                     () -> sut.getCommittersByRepositoryId(authentication, platformId, defaultBranch));
    }

    @Test
    void assignCommitterByRepositoryId_givenCommitter_returnsSavedItem() {
        // Given
        long platformId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        mockUserId(userId, authentication);

        CreateAssignmentDTO createAssignmentDTO = mock(CreateAssignmentDTO.class);

        // When
        sut.assignCommitters(authentication, platformId, createAssignmentDTO);

        // Then
        verify(repositoryService).assignCommitter(userId, platformId, createAssignmentDTO);
    }

    @Test
    void getAssignments_always_shouldCallGetAssignments() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        mockUserId(userId, authentication);

        // When
        sut.getAssignments(authentication, platformId);

        // Then
        verify(repositoryService).getAssignments(userId, platformId);
    }

    @Test
    void getAssignments_assignmentServiceReturnsEmptyList_shouldReturnEmptyList() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        mockUserId(userId, authentication);
        prepareGetAssignments(userId, platformId, List.of());

        // When
        List<AssignmentDTO> result = sut.getAssignments(authentication, platformId);

        // Then
        assertThat(result, equalTo(List.of()));
    }

    @Test
    void getAssignments_assignmentServiceReturnsSingleAssignment_shouldReturnSingleAssignmentDTO() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        Assignment assignment = mock(Assignment.class);
        AssignmentDTO mapperAssignment = mock(AssignmentDTO.class);
        mockUserId(userId, authentication);
        prepareGetAssignments(userId, platformId, List.of(assignment));
        prepareAssignmentMapper(List.of(assignment), List.of(mapperAssignment));

        // When
        List<AssignmentDTO> result = sut.getAssignments(authentication, platformId);

        // Then
        assertThat(result, containsInAnyOrder(mapperAssignment));
    }

    @Test
    void getAssignments_assignmentServiceReturnsMultipleAssignments_shouldReturnMultipleAssignmentDTOs() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        Assignment assignment1 = mock(Assignment.class);
        Assignment assignment2 = mock(Assignment.class);
        AssignmentDTO mapperAssignment1 = mock(AssignmentDTO.class);
        AssignmentDTO mapperAssignment2 = mock(AssignmentDTO.class);
        mockUserId(userId, authentication);
        prepareGetAssignments(userId, platformId, List.of(assignment1, assignment2));
        prepareAssignmentMapper(List.of(assignment1, assignment2), List.of(mapperAssignment1, mapperAssignment2));

        // When
        List<AssignmentDTO> result = sut.getAssignments(authentication, platformId);

        // Then
        assertThat(result, containsInAnyOrder(mapperAssignment1, mapperAssignment2));
    }

    @Test
    void deleteAssignment_always_shouldCallRepositoryServiceDeleteAssignment() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        long subAssignmentId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        mockUserId(userId, authentication);

        // When
        sut.deleteAssignment(authentication, platformId, subAssignmentId);

        // Then
        verify(repositoryService).deleteAssignment(userId, platformId, subAssignmentId);
    }

    private void prepareGetAllCommitters(long platformId, long userId, String branchName,
                                         Set<CommitterInternalDTO> output)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllCommitters(userId, platformId, branchName)).thenReturn(output);
    }

    private void prepareGetAllCommittersThrows(long platformId, long userId, String branch,
                                               Class<? extends Exception> exceptionClass)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllCommitters(userId, platformId, branch)).thenThrow(exceptionClass);
    }

    private void mockUserId(long userId, Authentication authentication) {
        when(authentication.getName()).thenReturn(String.valueOf(userId));
    }

    private void prepareGetAssignments(long userId, long platformId, List<Assignment> output) {
        when(repositoryService.getAssignments(userId, platformId)).thenReturn(output);
    }

    private void prepareGetAllCommits(long platformId, long userId, String branch, List<CommitInternalDTO> output)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllCommits(userId, platformId, branch)).thenReturn(output);
    }

    private void prepareGetAllCommitsThrows(long platformId, long userId, String branch,
                                            Class<? extends Exception> exceptionClass) throws GitException,
                                                                                              NoProviderFoundException {
        when(gitService.getAllCommits(userId, platformId, branch)).thenThrow(exceptionClass);
    }

    private void prepareGetAllBranches(long platformId, long userId, List<BranchInternalDTO> result)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllBranches(userId, platformId)).thenReturn(result);
    }

    private void prepareGetAllBranchesThrows(long platformId, long userId, Class<? extends Exception> exceptionClass)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllBranches(userId, platformId)).thenThrow(exceptionClass);
    }

    private void prepareGetRepositoryById(long platformId, long userId, NotSavedRepositoryInternalDTO output)
        throws GitException, NoProviderFoundException {
        when(gitService.getRepositoryById(userId, platformId)).thenReturn(output);
    }

    private void prepareGetRepositoryByIdThrows(long platformId, long userId, Class<? extends Exception> exceptionClass)
        throws GitException, NoProviderFoundException {
        when(gitService.getRepositoryById(userId, platformId)).thenThrow(exceptionClass);
    }

    private void prepareGetAllRepositories(long userId, List<NotSavedRepositoryInternalDTO> result)
        throws NoProviderFoundException, GitException {
        when(gitService.getAllRepositories(userId)).thenReturn(result);
    }

    private void prepareGetAllRepositoriesThrows(long userId, Class<? extends Exception> exceptionClass)
        throws NoProviderFoundException, GitException {
        when(gitService.getAllRepositories(userId)).thenThrow(exceptionClass);
    }

    private void prepareCommitterMapper(Set<CommitterInternalDTO> input, List<CommitterDTO> output) {
        when(committerMapper.dtosToDTOs(input)).thenReturn(output);
    }

    private void prepareAssignmentMapper(List<Assignment> input, List<AssignmentDTO> output) {
        when(assignmentMapper.entitiesToDTO(input)).thenReturn(output);
    }

    private void prepareCommitsMapper(List<CommitInternalDTO> input, List<CommitDTO> output) {
        when(commitMapper.dtosToDTOs(input)).thenReturn(output);
    }

    private void prepareBranchMapper(List<BranchInternalDTO> input, List<BranchDTO> output) {
        when(branchMapper.dtosToDTOs(input)).thenReturn(output);
    }

    private void prepareNotSavedRepositoryMapper(List<NotSavedRepositoryInternalDTO> input,
                                                 List<NotSavedRepositoryDTO> output) {
        when(notSavedRepositoryMapper.dtosToDTOs(input)).thenReturn(output);
    }

    private void prepareNotSavedRepositoryMapper(NotSavedRepositoryInternalDTO input, NotSavedRepositoryDTO output) {
        when(notSavedRepositoryMapper.dtoToDTO(input)).thenReturn(output);
    }
}
