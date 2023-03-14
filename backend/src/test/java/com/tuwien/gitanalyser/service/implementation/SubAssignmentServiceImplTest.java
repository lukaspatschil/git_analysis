package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.exception.ConflictException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.SubAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.util.Optional;

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

    @Test
    void deleteSubAssignmentById_subAssignmentExists_shouldDeleteSubAssignment() {
        // Given
        Long subAssignmentId = Randoms.getLong();
        SubAssignment subAssignment = mock(SubAssignment.class);
        when(subAssignmentRepository.findById(subAssignmentId)).thenReturn(Optional.of(subAssignment));

        // When
        sut.deleteSubAssignmentById(subAssignmentId);

        // Then
        verify(subAssignmentRepository).delete(subAssignment);
    }

    @Test
    void deleteSubAssignmentById_subAssignmentDoesNotExist_throwNotFoundException() {
        // Given
        Long subAssignmentId = Randoms.getLong();

        // When
        assertThrows(NotFoundException.class, () -> sut.deleteSubAssignmentById(subAssignmentId));

        // Then
    }
}