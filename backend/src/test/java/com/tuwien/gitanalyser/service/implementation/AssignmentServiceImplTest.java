package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.utils.AssignmentFactory;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.AssignmentRepository;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignmentServiceImplTest {

    AssignmentServiceImpl sut;
    private AssignmentRepository assignmentRepository;
    private AssignmentFactory assignmentFactory;
    private SubAssignmentService subAssignmentService;

    @BeforeEach
    void setUp() {
        subAssignmentService = mock(SubAssignmentService.class);
        assignmentRepository = mock(AssignmentRepository.class);
        assignmentFactory = mock(AssignmentFactory.class);
        sut = new AssignmentServiceImpl(subAssignmentService,
                                        assignmentRepository,
                                        assignmentFactory);
    }

    @Test
    void getOrCreateAssignment_assignmentExists_returnsAssignment() {
        // Given
        var repository = mock(Repository.class);
        var key = Randoms.alpha();
        var assignment = mock(Assignment.class);

        when(assignmentRepository.findByRepositoryAndKey(repository, key)).thenReturn(Optional.of(assignment));

        // When
        Assignment result = sut.getOrCreateAssignment(repository, key);

        // Then
        assertThat(result, equalTo(assignment));
    }

    @Test
    void getOrCreateAssignment_assignmentDoesNotExist_createsNewAssignment() {
        // Given
        var repository = mock(Repository.class);
        var key = Randoms.alpha();
        Assignment assignment = mockAssignmentFactory();
        when(assignmentRepository.findByRepositoryAndKey(repository, key)).thenReturn(Optional.empty());

        // When
        sut.getOrCreateAssignment(repository, key);

        // Then
        verify(assignmentRepository).save(assignment);
        assertThat(assignment.getKey(), equalTo(key));
        assertThat(assignment.getRepository(), equalTo(repository));
    }

    @Test
    void getOrCreateAssignment_assignmentDoesNotExist_returnsNewCreatedAssignment() {
        // Given
        var repository = mock(Repository.class);
        var key = Randoms.alpha();
        Assignment assignment = mockAssignmentFactory();
        var createdAssignment = mock(Assignment.class);

        when(assignmentRepository.findByRepositoryAndKey(repository, key)).thenReturn(Optional.empty());
        when(assignmentRepository.save(assignment)).thenReturn(createdAssignment);

        // When
        Assignment result = sut.getOrCreateAssignment(repository, key);

        // Then
        assertThat(result, equalTo(createdAssignment));
    }

    @Test
    void deleteSubAssignmentById_repositoryDoesNotExist_throwsNotFoundException() {
        // Given
        var repository = mock(Repository.class);
        var subAssignmentId = Randoms.getLong();
        when(assignmentRepository.findByRepositoryAndSubAssignments_Id(repository, subAssignmentId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> sut.deleteSubAssignmentById(repository, subAssignmentId));
    }

    @Test
    void deleteSubAssignmentById_repositoryExistsAndNoAssignments_throwsNotFoundException() {
        // Given
        var repository = mock(Repository.class);
        var subAssignmentId = Randoms.getLong();
        Assignment assignment = mock(Assignment.class);

        prepareFindByRepositoryAndSubAssignmentsId(repository, subAssignmentId, assignment);

        // When + Then
        assertThrows(NotFoundException.class, () ->sut.deleteSubAssignmentById(repository, subAssignmentId));
    }

    @Test
    void deleteSubAssignmentById_repositoryExistsAndOneSubAssignmentExists_deletesAssignment() {
        // Given
        var repository = mock(Repository.class);
        var subAssignmentId = Randoms.getLong();
        Assignment assignment = mock(Assignment.class);
        SubAssignment subAssignment = mock(SubAssignment.class);

        prepareFindByRepositoryAndSubAssignmentsId(repository, subAssignmentId, assignment, subAssignment);

        // When
        sut.deleteSubAssignmentById(repository, subAssignmentId);

        // Then
        verify(assignmentRepository).delete(assignment);
    }

    @Test
    void deleteSubAssignmentById_repositoryExistsAndMultipleSubAssignmentExist_deletesSubAssignment() {
        // Given
        var repository = mock(Repository.class);
        var subAssignmentId = Randoms.getLong();
        Assignment assignment = mock(Assignment.class);
        SubAssignment subAssignment1 = mock(SubAssignment.class);
        SubAssignment subAssignment2 = mock(SubAssignment.class);

        prepareFindByRepositoryAndSubAssignmentsId(repository, subAssignmentId, assignment, subAssignment1, subAssignment2);

        // When
        sut.deleteSubAssignmentById(repository, subAssignmentId);

        // Then
        verify(subAssignmentService).deleteSubAssignmentById(subAssignmentId);
    }

    private void prepareFindByRepositoryAndSubAssignmentsId(Repository repository, long subAssignmentId,
                                                            Assignment assignment, SubAssignment... subAssignments) {
        when(assignmentRepository.findByRepositoryAndSubAssignments_Id(repository, subAssignmentId))
            .thenReturn(Optional.of(assignment));
        when(assignment.getSubAssignments()).thenReturn(Arrays.asList(subAssignments));
    }

    private Assignment mockAssignmentFactory() {
        var assignment = new Assignment();
        when(assignmentFactory.create()).thenReturn(assignment);
        return assignment;
    }

}