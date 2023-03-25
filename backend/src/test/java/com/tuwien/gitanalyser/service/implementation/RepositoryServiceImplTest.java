package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.CreateAssignmentDTOs;
import utils.Randoms;

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

class RepositoryServiceImplTest {
    private RepositoryServiceImpl sut;
    private UserService userService;
    private RepositoryRepository repositoryRepository;
    private AssignmentService assignmentService;
    private SubAssignmentService subAssignmentService;
    private RepositoryFactory repositoryFactory;

    private static Repository createRepository() {
        return Repository.builder().platformId(Randoms.getLong()).build();
    }

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        repositoryRepository = mock(RepositoryRepository.class);
        assignmentService = mock(AssignmentService.class);
        subAssignmentService = mock(SubAssignmentService.class);
        repositoryFactory = mock(RepositoryFactory.class);
        sut = new RepositoryServiceImpl(userService,
                                        repositoryRepository,
                                        assignmentService,
                                        subAssignmentService,
                                        repositoryFactory
        );
    }

    @Test
    void assignCommitters_always_shouldNotThrowException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();

        prepareRepositoryFactory(repositoryEntity);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);
    }

    @Test
    void assignCommitters_repositoryDoesNotExist_createsRepository() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();

        prepareRepositoryFactory(repositoryEntity);
        mockRepositoryFindByUserAndPlatformIdEmpty(user, platformId);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository).save(repositoryEntity);
    }

    @Test
    void assignCommitters_repositoryExists_shouldNotCreateRepository() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository, never()).save(any());
    }

    @Test
    void assignCommitters_repositoryExists_shouldAddSubAssignmentToAssignment() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();
        Assignment assignment = new Assignment();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        prepareAssignment(repository, createDTO, assignment);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(subAssignmentService).addSubAssignment(assignment, createDTO.getAssignedName());
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
        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        return repository;
    }
}
