package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.SubAssignmentFactory;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.ForbiddenException;
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
    private GitService gitService;
    private RepositoryRepository repositoryRepository;
    private AssignmentService assignmentService;
    private SubAssignmentService subAssignmentService;
    private SubAssignmentFactory subAssignmentFactory;
    private RepositoryFactory repositoryFactory;

    @BeforeEach
    void setUp() {
        gitService = mock(GitService.class);
        userService = mock(UserService.class);
        repositoryRepository = mock(RepositoryRepository.class);
        assignmentService = mock(AssignmentService.class);
        subAssignmentService = mock(SubAssignmentService.class);
        subAssignmentFactory = mock(SubAssignmentFactory.class);
        repositoryFactory = mock(RepositoryFactory.class);
        sut = new RepositoryServiceImpl(userService,
                                        gitService,
                                        repositoryRepository,
                                        assignmentService,
                                        subAssignmentService,
                                        subAssignmentFactory,
                                        repositoryFactory);
    }

    @Test
    void assignCommitters_accessNotAllowed_shouldThrowForbiddenException() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();

        // When + Then
        assertThrows(ForbiddenException.class, () -> sut.assignCommitter(userId, platformId, createDTO));
    }

    @Test
    void assignCommitters_accessAllowed_shouldNotThrowException() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();
        SubAssignment subAssignment = new SubAssignment();

        prepareRepositoryAvailable(user, platformId);
        prepareFactories(repositoryEntity, subAssignment);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);
    }

    @Test
    void assignCommitters_accessAllowedAndRepositoryDoesNotExist_createsRepository() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repositoryEntity = new Repository();
        SubAssignment subAssignment = new SubAssignment();

        prepareRepositoryAvailable(user, platformId);
        prepareFactories(repositoryEntity, subAssignment);
        mockRepositoryFindByUserAndPlatformIdEmpty(user, platformId);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository).save(repositoryEntity);
    }

    @Test
    void assignCommitters_accessAllowedAndRepositoryExists_shouldNotCreateRepository() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();
        SubAssignment subAssignment = new SubAssignment();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        prepareRepositoryAvailable(user, platformId);
        prepareSubAssignmentFactory(subAssignment);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(repositoryRepository, never()).save(any());
    }

    @Test
    void assignCommitters_accessAllowedAndRepositoryExists_shouldAddSubAssignmentToAssignment() {
        // Given
        User user = prepareUserService();
        long platformId = Randoms.getLong();
        CreateAssignmentDTO createDTO = CreateAssignmentDTOs.random();
        Repository repository = new Repository();
        SubAssignment subAssignment = new SubAssignment();
        Assignment assignment = new Assignment();

        mockRepositoryFindByUserAndPlatformId(user, platformId, repository);
        prepareRepositoryAvailable(user, platformId);
        prepareAssignment(repository, createDTO, assignment);
        prepareSubAssignmentFactory(subAssignment);

        // When
        sut.assignCommitter(user.getId(), platformId, createDTO);

        // Then
        verify(subAssignmentService).addSubAssignment(assignment, subAssignment);
    }

    @Test
    void getAssignments_userIsNotAllowedToAccess_shouldThrowExceptionForbiddenException() {
        // Given
        long userId = Randoms.getLong();
        long platformId = Randoms.getLong();

        // When + Then
        assertThrows(ForbiddenException.class, () -> sut.getAssignments(userId, platformId));
    }

    @Test
    void getAssignments_userIsAllowedToAccessAndRepositoryExistsAndAssignmentExists_returnsAssignment() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();
        prepareRepositoryAvailable(user, platformId);

        Assignment assignment = mock(Assignment.class);
        prepareExistingRepository(List.of(assignment), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, containsInAnyOrder(assignment));
    }

    @Test
    void getAssignments_userIsAllowedToAccessAndRepositoryExistsAndMultipleAssignmentExists_returnsAssignments() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();
        prepareRepositoryAvailable(user, platformId);

        Assignment assignment1 = mock(Assignment.class);
        Assignment assignment2 = mock(Assignment.class);

        prepareExistingRepository(List.of(assignment1, assignment2), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, containsInAnyOrder(assignment1, assignment2));
    }

    @Test
    void getAssignments_userIsAllowedToAccessAndRepositoryExistsAndNoAssignmentExists_returnsEmptyList() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();
        prepareRepositoryAvailable(user, platformId);

        prepareExistingRepository(List.of(), platformId, user);

        // When
        List<Assignment> result = sut.getAssignments(user.getId(), platformId);

        // Then
        assertThat(result, equalTo(List.of()));
    }

    @Test
    void getAssignments_userIsAllowedToAccessAndRepositoryDoesNotExist_throwsNotFoundException() {
        // Given
        long platformId = Randoms.getLong();

        User user = prepareUserService();
        prepareRepositoryAvailable(user, platformId);

        // When + Then
        assertThrows(NotFoundException.class, () -> sut.getAssignments(user.getId(), platformId));

    }

    private void prepareAssignment(Repository repository, CreateAssignmentDTO createDTO, Assignment assignment) {
        when(assignmentService.getOrCreateAssignment(repository, createDTO.getKey())).thenReturn(assignment);
    }

    private void prepareFactories(Repository repositoryEntity, SubAssignment subAssignmentEntity) {
        prepareRepositoryFactory(repositoryEntity);
        prepareSubAssignmentFactory(subAssignmentEntity);
    }

    private void prepareRepositoryAvailable(User user, long platformId) {
        when(gitService.repositoryAccessibleByUser(user.getId(), platformId)).thenReturn(true);
    }

    private void mockRepositoryFindByUserAndPlatformId(User user, long platformId, Repository repository) {
        when(repositoryRepository.findByUserAndPlatformId(user, platformId)).thenReturn(Optional.of(repository));
    }

    private void mockRepositoryFindByUserAndPlatformIdEmpty(User user, long platformId) {
        when(repositoryRepository.findByUserAndPlatformId(user, platformId)).thenReturn(Optional.empty());
    }

    private User prepareUserService()
        throws NotFoundException {
        Long userId = Randoms.getLong();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(userService.getUser(userId)).thenReturn(user);
        return user;
    }

    private SubAssignment prepareSubAssignmentFactory(SubAssignment subAssignment) {
        when(subAssignmentFactory.create()).thenReturn(subAssignment);
        return subAssignment;
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
