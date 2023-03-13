package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.utils.AssignmentFactory;
import com.tuwien.gitanalyser.repository.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignmentServiceImplTest {

    AssignmentServiceImpl sut;
    private AssignmentRepository assignmentRepository;
    private AssignmentFactory assignmentFactory;

    @BeforeEach
    void setUp() {
        assignmentRepository = mock(AssignmentRepository.class);
        assignmentFactory = mock(AssignmentFactory.class);
        sut = new AssignmentServiceImpl(assignmentRepository, assignmentFactory);
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

    private Assignment mockAssignmentFactory() {
        var assignment = new Assignment();
        when(assignmentFactory.create()).thenReturn(assignment);
        return assignment;
    }
}