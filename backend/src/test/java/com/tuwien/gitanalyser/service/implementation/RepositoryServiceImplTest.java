package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.IllegalArgumentException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.CreateAssignmentDTOs;
import utils.Randoms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.Matchers.commitInteralDTOMatcher;
import static utils.Matchers.statsInternalDTOMatcher;

class RepositoryServiceImplTest {
    Date DATE = new Date();
    private RepositoryServiceImpl sut;
    private UserService userService;
    private RepositoryRepository repositoryRepository;
    private AssignmentService assignmentService;
    private SubAssignmentService subAssignmentService;
    private RepositoryFactory repositoryFactory;
    private GitService gitService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        repositoryRepository = mock(RepositoryRepository.class);
        assignmentService = mock(AssignmentService.class);
        subAssignmentService = mock(SubAssignmentService.class);
        repositoryFactory = mock(RepositoryFactory.class);
        gitService = mock(GitService.class);
        sut = new RepositoryServiceImpl(userService,
                                        repositoryRepository,
                                        assignmentService,
                                        subAssignmentService,
                                        gitService,
                                        repositoryFactory
        );
    }

    @Test
    void addAssignment_always_shouldNotThrowException() throws IllegalArgumentException {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();

        prepareRepositoryFactory(repositoryEntity);

        // When
        sut.addAssignment(user.getId(), platformId, createDTO);
    }

    @Test
    void addAssignment_repositoryDoesNotExist_createsRepository() throws IllegalArgumentException {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();

        prepareRepositoryFactory(repositoryEntity);
        mockRepositoryFindByUserAndPlatformIdEmpty(user, platformId);

        // When
        sut.addAssignment(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository).save(repositoryEntity);
    }

    @Test
    void addAssignment_repositoryExists_shouldNotCreateRepository() throws IllegalArgumentException {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When
        sut.addAssignment(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository, never()).save(any());
    }

    @Test
    void addAssignment_repositoryExists_shouldAddSubAssignmentToAssignment() throws IllegalArgumentException {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();
        Assignment assignment = new Assignment();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        prepareAssignment(repository, createDTO, assignment);

        // When
        sut.addAssignment(user.getId(), platformId, createDTO);

        // Then
        verify(subAssignmentService).addSubAssignment(assignment, createDTO.getAssignedName());
    }

    @Test
    void addAssignment_repositoryExistsAndKeyAndAssignedNameIsTheSame_throwIllegalArgumentException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        String keyAndAssignedName = Randoms.alpha();
        CreateAssignmentDTO createDTO = CreateAssignmentDTO.builder()
                                                           .key(keyAndAssignedName)
                                                           .assignedName(keyAndAssignedName)
                                                           .build();
        Repository repository = new Repository();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> sut.addAssignment(user.getId(), platformId, createDTO));
    }

    @Test
    void addAssignment_repositoryExistsAndNameIsAlreadyAssignedToASubAssignment_throwIllegalArgumentException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        CreateAssignmentDTO createDTO = CreateAssignmentDTO.builder()
                                                           .key(key)
                                                           .assignedName(assignedName)
                                                           .build();
        Repository repository = new Repository();
        Assignment assignment =
            Assignment.builder()
                      .key(Randoms.alpha())
                      .subAssignments(List.of(SubAssignment.builder().assignedName(assignedName).build()))
                      .build();
        repository.setAssignments(List.of(assignment));
        prepareAssignment(repository, createDTO, assignment);

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> sut.addAssignment(user.getId(), platformId, createDTO));
    }

    @Test
    void addAssignment_repositoryExistsAndNameIsAlreadyAssignedToKey_throwIllegalArgumentException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        CreateAssignmentDTO createDTO = CreateAssignmentDTO.builder()
                                                           .key(key)
                                                           .assignedName(assignedName)
                                                           .build();
        Repository repository = new Repository();
        Assignment assignment =
            Assignment.builder()
                      .key(assignedName)
                      .subAssignments(List.of(SubAssignment.builder().assignedName(Randoms.alpha()).build()))
                      .build();
        repository.setAssignments(List.of(assignment));
        prepareAssignment(repository, createDTO, assignment);

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> sut.addAssignment(user.getId(), platformId, createDTO));
    }

    @Test
    void addAssignment_repositoryExistsAndKeyIsAssignedToASubAssignment_throwIllegalArgumentException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        String key = Randoms.alpha();
        String assignedName = Randoms.alpha();
        CreateAssignmentDTO createDTO = CreateAssignmentDTO.builder()
                                                           .key(key)
                                                           .assignedName(assignedName)
                                                           .build();
        Repository repository = new Repository();
        Assignment assignment =
            Assignment.builder()
                      .key(Randoms.alpha())
                      .subAssignments(List.of(SubAssignment.builder().assignedName(key).build()))
                      .build();
        repository.setAssignments(List.of(assignment));
        prepareAssignment(repository, createDTO, assignment);

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> sut.addAssignment(user.getId(), platformId, createDTO));
    }

    @Test
    void getAssignments_repositoryExistsAndAssignmentExists_returnsAssignment() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        Assignment assignment = mock(Assignment.class);
        prepareExistingRepository(List.of(assignment), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, containsInAnyOrder(assignment));
    }

    @Test
    void getAssignments_repositoryExistsAndMultipleAssignmentExists_returnsAssignments() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        Assignment assignment1 = mock(Assignment.class);
        Assignment assignment2 = mock(Assignment.class);

        prepareExistingRepository(List.of(assignment1, assignment2), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, containsInAnyOrder(assignment1, assignment2));
    }

    @Test
    void getAssignments_repositoryExistsAndNoAssignmentExists_returnsEmptyList() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        prepareExistingRepository(List.of(), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, equalTo(List.of()));
    }

    @Test
    void getAssignments_repositoryDoesNotExist_throwsNotFoundException() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        // When + Then
        assertThrows(NotFoundException.class, () -> sut.getAssignments(user.getId(), platformId));

    }

    @Test
    void deleteAllNotAccessibleRepositoryEntities_singleRepoSavedAndNoRepoFound_deletesRepo() {
        // Given
        Repository repository = createRepository();

        User user = prepareUserService();
        when(repositoryRepository.findByUser(user)).thenReturn(List.of(repository));

        // When
        sut.deleteAllNotAccessibleRepositoryEntities(user.getId(), List.of());

        // Then
        verify(repositoryRepository).delete(repository);
    }

    @Test
    void deleteAllNotAccessibleRepositoryEntities_singleRepoSavedAndSameRepoFound_doesNotDeleteRepo() {
        // Given
        Repository repository = createRepository();

        User user = prepareUserService();
        when(repositoryRepository.findByUser(user)).thenReturn(List.of(repository));

        // When
        sut.deleteAllNotAccessibleRepositoryEntities(user.getId(), List.of(repository.getPlatformId()));

        // Then
        verify(repositoryRepository, never()).delete(repository);
    }

    @Test
    void deleteAllNotAccessibleRepositoryEntities_TwoReposSavedAndOneRepoFound_deleteNotFoundRepo() {
        // Given
        Repository repository1 = createRepository();
        Repository repository2 = createRepository();

        User user = prepareUserService();
        when(repositoryRepository.findByUser(user)).thenReturn(List.of(repository1, repository2));

        // When
        sut.deleteAllNotAccessibleRepositoryEntities(user.getId(), List.of(repository1.getPlatformId()));

        // Then
        verify(repositoryRepository).delete(repository2);
    }

    @Test
    void deleteAllNotAccessibleRepositoryEntities_TwoReposSavedAndNoRepoFound_deleteAllRepos() {
        // Given
        Repository repository1 = createRepository();
        Repository repository2 = createRepository();

        User user = prepareUserService();
        when(repositoryRepository.findByUser(user)).thenReturn(List.of(repository1, repository2));

        // When
        sut.deleteAllNotAccessibleRepositoryEntities(user.getId(), List.of());

        // Then
        verify(repositoryRepository).delete(repository1);
        verify(repositoryRepository).delete(repository2);
    }

    @Test
    void getStats_repositoryExistsAndAssignmentExistsAndShouldNotBeMapped_returnsUnmappedStats()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        String branch = Randoms.alpha();

        Assignment assignment = mock(Assignment.class);
        prepareExistingRepository(List.of(assignment), platformId, user);

        StatsInternalDTO stats1 = mockStatsInternalDTO();
        StatsInternalDTO stats2 = mockStatsInternalDTO();

        prepareGitServiceGetStats(platformId, user, branch, List.of(stats1, stats2));

        // When
        List<StatsInternalDTO> result = sut.getStats(user.getId(), platformId, branch, false);

        // Then
        assertThat(result, containsInAnyOrder(stats1, stats2));
    }

    @Test
    void getStats_oneStatsObjectAndShouldBeMapped_returnsMappedStats()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName);

        StatsInternalDTO stats = mockStatsInternalDTO(assignedName);

        StatsInternalDTO resultStats = mockStatsInternalDTO(key, stats);

        prepareGitServiceGetStats(platformId, user, branch, List.of(stats));

        // When
        List<StatsInternalDTO> result = sut.getStats(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(statsInternalDTOMatcher(resultStats)));
    }

    @Test
    void getStats_twoStatsObjectAndShouldBeMapped_returnsMappedStats()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName1 = Randoms.alpha();
        String assignedName2 = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName1, assignedName2);

        StatsInternalDTO stats1 = mockStatsInternalDTO(assignedName1);
        StatsInternalDTO stats2 = mockStatsInternalDTO(assignedName2);

        StatsInternalDTO resultStats = mockStatsInternalDTO(key, stats1, stats2);

        prepareGitServiceGetStats(platformId, user, branch, List.of(stats1, stats2));

        // When
        List<StatsInternalDTO> result = sut.getStats(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(
            statsInternalDTOMatcher(resultStats)
        ));
    }

    @Test
    void getStats_threeStatsObjectAnd2ShouldBeMapped_returnsMappedStats()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName1 = Randoms.alpha();
        String assignedName2 = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName1, assignedName2);

        StatsInternalDTO stats1 = mockStatsInternalDTO(assignedName1);
        StatsInternalDTO stats2 = mockStatsInternalDTO(assignedName2);
        StatsInternalDTO stats3 = mockStatsInternalDTO();

        StatsInternalDTO resultStats = mockStatsInternalDTO(key, stats1, stats2);

        prepareGitServiceGetStats(platformId, user, branch, List.of(stats1, stats2, stats3));

        // When
        List<StatsInternalDTO> result = sut.getStats(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(
            statsInternalDTOMatcher(resultStats),
            statsInternalDTOMatcher(stats3)
        ));
    }

    @Test
    void getCommits_repositoryExistsAndAssignmentExistsAndShouldNotBeMapped_returnsUnmappedCommits()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();

        String branch = Randoms.alpha();

        Assignment assignment = mock(Assignment.class);
        prepareExistingRepository(List.of(assignment), platformId, user);

        CommitInternalDTO commit1 = mockCommitInternalDTO();
        CommitInternalDTO commit2 = mockCommitInternalDTO();

        prepareGitServiceGetCommits(platformId, user, branch, List.of(commit1, commit2));

        // When
        List<CommitInternalDTO> result = sut.getCommits(user.getId(), platformId, branch, false);

        // Then
        assertThat(result, containsInAnyOrder(commitInteralDTOMatcher(commit1), commitInteralDTOMatcher(commit2)));
    }

    @Test
    void getCommits_oneStatsObjectAndShouldBeMapped_returnsMappedCommits()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName);

        CommitInternalDTO commit = mockCommitInternalDTO(assignedName);

        CommitInternalDTO resultCommit = mockCommitInternalDTO(key, commit);

        prepareGitServiceGetCommits(platformId, user, branch, List.of(commit));

        // When
        List<CommitInternalDTO> result = sut.getCommits(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(commitInteralDTOMatcher(resultCommit)));
    }

    @Test
    void getCommits_twoStatsObjectAndShouldBeMapped_returnsMappedCommits()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName1 = Randoms.alpha();
        String assignedName2 = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName1, assignedName2);

        CommitInternalDTO commit1 = mockCommitInternalDTO(assignedName1);
        CommitInternalDTO commit2 = mockCommitInternalDTO(assignedName2);

        CommitInternalDTO resultCommit1 = mockCommitInternalDTO(key, commit1);
        CommitInternalDTO resultCommit2 = mockCommitInternalDTO(key, commit2);

        prepareGitServiceGetCommits(platformId, user, branch, List.of(commit1, commit2));

        // When
        List<CommitInternalDTO> result = sut.getCommits(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(commitInteralDTOMatcher(resultCommit1),
                                              commitInteralDTOMatcher(resultCommit2)
        ));
    }

    @Test
    void getCommits_threeStatsObjectAnd2ShouldBeMapped_returnsMappedCommits()
        throws GitException, NoProviderFoundException {
        // Given
        long platformId = Randoms.getLong();
        String branch = Randoms.alpha();
        String assignedName1 = Randoms.alpha();
        String assignedName2 = Randoms.alpha();
        String key = Randoms.alpha();

        User user = prepareUserService();

        prepareAssignments(platformId, key, user, assignedName1, assignedName2);

        CommitInternalDTO commit1 = mockCommitInternalDTO(assignedName1);
        CommitInternalDTO commit2 = mockCommitInternalDTO(assignedName2);
        CommitInternalDTO commit3 = mockCommitInternalDTO();

        CommitInternalDTO resultCommit1 = mockCommitInternalDTO(key, commit1);
        CommitInternalDTO resultCommit2 = mockCommitInternalDTO(key, commit2);

        prepareGitServiceGetCommits(platformId, user, branch, List.of(commit1, commit2, commit3));

        // When
        List<CommitInternalDTO> result = sut.getCommits(user.getId(), platformId, branch, true);

        // Then
        assertThat(result, containsInAnyOrder(
            commitInteralDTOMatcher(resultCommit1),
            commitInteralDTOMatcher(resultCommit2),
            commitInteralDTOMatcher(commit3)
        ));
    }

    private void prepareAssignments(long platformId, String key, User user, String... assignedNames) {
        Assignment assignment = createAssignment(key);
        List<SubAssignment> subAssignments = new ArrayList<>();
        for (String assignedName : assignedNames) {
            subAssignments.add(mockSubAssignment(assignedName, assignment));
        }
        when(assignment.getSubAssignments()).thenReturn(subAssignments);
        prepareExistingRepository(List.of(assignment), platformId, user);
    }

    private void prepareGitServiceGetStats(long platformId, User user, String branch, List<StatsInternalDTO> stats)
        throws NoProviderFoundException, GitException {
        when(gitService.getStats(user.getId(), platformId, branch)).thenReturn(stats);
    }

    private void prepareAssignment(Repository repository, CreateAssignmentDTO createDTO, Assignment assignment) {
        when(assignmentService.getOrCreateAssignment(repository, createDTO.getKey())).thenReturn(assignment);
    }

    private void mockRepositoryFindByUserAndPlatformId(User user, long platformId, Repository repository) {
        when(repositoryRepository.findByUserAndPlatformId(user, platformId)).thenReturn(Optional.of(repository));
    }

    private void mockRepositoryFindByUserAndPlatformIdEmpty(User user, long platformId) {
        when(repositoryRepository.findByUserAndPlatformId(user, platformId)).thenReturn(Optional.empty());
    }

    private User prepareUserService() throws NotFoundException {
        Long userId = Randoms.getLong();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(userService.getUser(userId)).thenReturn(user);
        return user;
    }

    private void prepareRepositoryFactory(Repository repository) {
        when(repositoryFactory.create()).thenReturn(repository);
    }

    private Repository prepareExistingRepository(List<Assignment> assignments, long platformId, User user) {
        Repository repository = mock(Repository.class);
        when(repository.getAssignments()).thenReturn(assignments);
        when(repository.getUser()).thenReturn(user);
        when(repository.getPlatformId()).thenReturn(platformId);
        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        return repository;
    }

    private Repository createRepository() {
        return Repository.builder().platformId(Randoms.getLong()).build();
    }

    private StatsInternalDTO mockStatsInternalDTO(String name) {
        StatsInternalDTO statsInternalDTO = mock(StatsInternalDTO.class);
        when(statsInternalDTO.getCommitter()).thenReturn(name);
        when(statsInternalDTO.getNumberOfCommits()).thenReturn(Randoms.integer(0, 10));
        when(statsInternalDTO.getNumberOfDeletions()).thenReturn(Randoms.integer(0, 10));
        when(statsInternalDTO.getNumberOfAdditions()).thenReturn(Randoms.integer(0, 10));
        return statsInternalDTO;
    }

    private StatsInternalDTO mockStatsInternalDTO(String name, StatsInternalDTO... stats) {
        StatsInternalDTO statsInternalDTO = mock(StatsInternalDTO.class);
        when(statsInternalDTO.getCommitter()).thenReturn(name);

        int numberOfCommits = 0;
        int numberOfDeletions = 0;
        int numberOfAdditions = 0;

        for (StatsInternalDTO stat : stats) {
            numberOfCommits += stat.getNumberOfCommits();
            numberOfAdditions += stat.getNumberOfAdditions();
            numberOfDeletions += stat.getNumberOfDeletions();
        }

        when(statsInternalDTO.getNumberOfCommits()).thenReturn(numberOfCommits);
        when(statsInternalDTO.getNumberOfDeletions()).thenReturn(numberOfDeletions);
        when(statsInternalDTO.getNumberOfAdditions()).thenReturn(numberOfAdditions);
        return statsInternalDTO;
    }

    private StatsInternalDTO mockStatsInternalDTO() {
        return mockStatsInternalDTO(Randoms.alpha());
    }

    private SubAssignment mockSubAssignment(String assignedName, Assignment assignment) {
        SubAssignment subAssignment = mock(SubAssignment.class);
        when(subAssignment.getAssignedName()).thenReturn(assignedName);
        when(subAssignment.getAssignment()).thenReturn(assignment);
        return subAssignment;
    }

    private Assignment createAssignment(String key) {
        Assignment assignment = mock(Assignment.class);
        when(assignment.getKey()).thenReturn(key);

        return assignment;
    }

    private CommitInternalDTO mockCommitInternalDTO(String name) {

        return CommitInternalDTO.builder()
                                .author(name)
                                .additions(Randoms.integer(0, 10))
                                .deletions(Randoms.integer(0, 10))
                                .message(Randoms.alpha())
                                .timestamp(DATE)
                                .parentIds(List.of(Randoms.alpha(), Randoms.alpha()))
                                .isMergeCommit(true)
                                .id(Randoms.alpha())
                                .build();
    }

    private CommitInternalDTO mockCommitInternalDTO(String name, CommitInternalDTO refCommit) {

        return CommitInternalDTO.builder()
                                .author(name)
                                .additions(refCommit.getAdditions())
                                .deletions(refCommit.getDeletions())
                                .message(refCommit.getMessage())
                                .timestamp(refCommit.getTimestamp())
                                .parentIds(refCommit.getParentIds())
                                .isMergeCommit(refCommit.isMergeCommit())
                                .id(refCommit.getId())
                                .build();
    }

    private CommitInternalDTO mockCommitInternalDTO() {
        return mockCommitInternalDTO(Randoms.alpha());
    }

    private void prepareGitServiceGetCommits(long platformId, User user, String branch, List<CommitInternalDTO> commits)
        throws GitException, NoProviderFoundException {
        when(gitService.getAllCommits(user.getId(), platformId, branch)).thenReturn(commits);
    }
}
