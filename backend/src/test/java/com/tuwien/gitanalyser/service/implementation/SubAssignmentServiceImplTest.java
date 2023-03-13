package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.exception.ConflictException;
import com.tuwien.gitanalyser.repository.SubAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubAssignmentServiceImplTest {

    private SubAssignmentServiceImpl sut;
    private SubAssignmentRepository subAssignmentRepository;

    @BeforeEach
    void setUp() {
        subAssignmentRepository = mock(SubAssignmentRepository.class);
        sut = new SubAssignmentServiceImpl(subAssignmentRepository);
    }

    @Test
    void addSubAssignment_always_shouldStoreSubAssignment() {
        // Given
        Assignment assignment = mock(Assignment.class);
        SubAssignment subAssignment = mock(SubAssignment.class);

        // When
        sut.addSubAssignment(assignment, subAssignment);

        // Then
        verify(subAssignmentRepository).save(subAssignment);
    }

    @Test
    void addSubAssignment_onSaveThrowException_throwsConflictException() {
        // Given
        Assignment assignment = mock(Assignment.class);
        SubAssignment subAssignment = mock(SubAssignment.class);

        // When
        when(subAssignmentRepository.save(subAssignment)).thenThrow(new RuntimeException("Assignment already exists"));

        // Then
        assertThrows(ConflictException.class, () -> sut.addSubAssignment(assignment, subAssignment));
    }
}