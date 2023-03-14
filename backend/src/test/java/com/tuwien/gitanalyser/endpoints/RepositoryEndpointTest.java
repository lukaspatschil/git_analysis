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
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import utils.Randoms;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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
    void getAllRepositories_always_shouldCallService() {
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
    void getAllRepositories_serviceReturnsListOfRepositories_returnList() {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getAllRepositories(userId)).thenReturn(List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3));

        when(notSavedRepositoryMapper.dtosToDTOs(List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3)))
            .thenReturn(List.of(
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
    void getAllRepositories_serviceReturnsEmptyListOfRepositories_returnList() {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, equalTo(List.of()));
    }

    @Test
    void getRepositoryById_always_shouldCallService() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(Randoms.getLong(), authentication);

        // When
        sut.getRepositoryById(authentication, repositoryId);

        // Then
        verify(gitService).getRepositoryById(Long.parseLong(authentication.getName()), repositoryId);
    }

    @Test
    void getRepositoryById_givenOneRepository_returnsRepository() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getRepositoryById(userId, repositoryId)).thenReturn(NOT_SAVED_REPOSITORY_INTERNAL_DTO_1);
        when(notSavedRepositoryMapper.dtoToDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_1)).thenReturn(
            NOT_SAVED_REPOSITORY_DTO_1);

        // When
        var result = sut.getRepositoryById(authentication, repositoryId);

        // Then
        assertThat(result, equalTo(NOT_SAVED_REPOSITORY_DTO_1));
    }

    @Test
    void getBranchesByRepositoryId_always_shouldCallService() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        verify(gitService).getAllBranches(userId, repositoryId);
    }

    @Test
    void getBranchesByRepositoryId_givenOneBranch_returnsListWithOneItem() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getAllBranches(userId, repositoryId)).thenReturn(List.of(BRANCH_INTERNAL_DTO_1));
        when(branchMapper.dtosToDTOs(List.of(BRANCH_INTERNAL_DTO_1))).thenReturn(List.of(BRANCH_DTO_1));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1));
    }

    @Test
    void getBranchesByRepositoryId_givenTwoBranches_returnsListWithTwoItems() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getAllBranches(userId, repositoryId)).thenReturn(List.of(
            BRANCH_INTERNAL_DTO_1, BRANCH_INTERNAL_DTO_2));
        when(branchMapper.dtosToDTOs(List.of(
            BRANCH_INTERNAL_DTO_1,
            BRANCH_INTERNAL_DTO_2)
        )).thenReturn(List.of(
            BRANCH_DTO_1,
            BRANCH_DTO_2));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1, BRANCH_DTO_2));
    }

    @Test
    void getCommitsByRepositoryId_always_shouldCallService() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getCommitsByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        verify(gitService).getAllCommits(userId, repositoryId, defaultBranch);
    }

    @Test
    void getCommitsByRepositoryId_givenOneCommit_returnsListWithOneCommit() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        CommitInternalDTO commit = mock(CommitInternalDTO.class);
        CommitDTO commitDTO = mock(CommitDTO.class);
        when(gitService.getAllCommits(userId, repositoryId, defaultBranch)).thenReturn(List.of(commit));
        when(commitMapper.dtosToDTOs(List.of(commit))).thenReturn(List.of(commitDTO));

        // When
        List<CommitDTO> commits = sut.getCommitsByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        assertThat(commits, containsInAnyOrder(commitDTO));
    }

    @Test
    void getCommitsByRepositoryId_givenTwoCommits_returnsListWithTwoCommits() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        when(gitService.getAllCommits(userId, repositoryId, defaultBranch)).thenReturn(List.of(
            commit1, commit2));
        when(commitMapper.dtosToDTOs(List.of(
            commit1,
            commit2)
        )).thenReturn(List.of(
            commitDTO1,
            commitDTO2));

        // When
        List<CommitDTO> branches = sut.getCommitsByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        assertThat(branches, equalTo(List.of(commitDTO1, commitDTO2)));
    }

    @Test
    void getCommittersByRepositoryId_always_shouldCallService() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        // When
        sut.getCommittersByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        verify(gitService).getAllCommitters(userId, repositoryId, defaultBranch);
    }

    @Test
    void getCommittersByRepositoryId_givenOneCommitter_returnsListWithOneItem() throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);

        mockGetAllCommitters(repositoryId, userId, defaultBranch, Set.of(COMMITTER_INTERNAL_DTO_1));
        mockCommitterMapper(Set.of(COMMITTER_INTERNAL_DTO_1), List.of(COMMITTER_DTO_1));

        // When
        List<CommitterDTO> result = sut.getCommittersByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(COMMITTER_DTO_1));
    }

    @Test
    void assignCommitterByRepositoryId_givenCommitter_returnsSavedItem() {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);
        mockUserId(userId, authentication);

        CreateAssignmentDTO createAssignmentDTO = mock(CreateAssignmentDTO.class);

        // When
        sut.assignCommitters(authentication, repositoryId, createAssignmentDTO);

        // Then
        verify(repositoryService).assignCommitter(userId, repositoryId, createAssignmentDTO);
    }

    @Test
    void getCommittersByRepositoryId_givenTwoCommitters_returnsListWithTwoItems() throws GitLabApiException,
                                                                                         IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        mockUserId(userId, authentication);
        mockGetAllCommitters(repositoryId, userId, defaultBranch, Set.of(
            COMMITTER_INTERNAL_DTO_1, COMMITTER_INTERNAL_DTO_2));
        mockCommitterMapper(Set.of(
            COMMITTER_INTERNAL_DTO_1,
            COMMITTER_INTERNAL_DTO_2), List.of(
            COMMITTER_DTO_1,
            COMMITTER_DTO_2));

        // When
        List<CommitterDTO> result = sut.getCommittersByRepositoryId(authentication, repositoryId, defaultBranch);

        // Then
        assertThat(result, containsInAnyOrder(COMMITTER_DTO_1, COMMITTER_DTO_2));
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
        when(repositoryService.getAssignments(userId, platformId)).thenReturn(List.of());

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
        when(repositoryService.getAssignments(userId, platformId)).thenReturn(List.of(assignment));
        when(assignmentMapper.entitiesToDTO(List.of(assignment))).thenReturn(List.of(mapperAssignment));

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
        when(repositoryService.getAssignments(userId, platformId)).thenReturn(List.of(assignment1, assignment2));
        when(assignmentMapper.entitiesToDTO(List.of(assignment1, assignment2)))
            .thenReturn(List.of(mapperAssignment1, mapperAssignment2));

        // When
        List<AssignmentDTO> result = sut.getAssignments(authentication, platformId);

        // Then
        assertThat(result, containsInAnyOrder(mapperAssignment1, mapperAssignment2));
    }


    @Test
    void deleteAssignment_always_shouldCallRepositoryServiceDeleteAssignment(){
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
    private void mockGetAllCommitters(long repositoryId, long userId, String branchName,
                                      Set<CommitterInternalDTO> committerInternalDto)
        throws GitLabApiException, IOException {
        when(gitService.getAllCommitters(userId, repositoryId, branchName)).thenReturn(committerInternalDto);
    }

    private void mockCommitterMapper(Set<CommitterInternalDTO> committerInternalDto, List<CommitterDTO> committerDto) {
        when(committerMapper.dtosToDTOs(committerInternalDto
        )).thenReturn(committerDto);
    }

    private void mockUserId(long userId, Authentication authentication) {
        when(authentication.getName()).thenReturn(String.valueOf(userId));
    }

}
