package com.tuwien.gitanalyser.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class SubAssignmentFactoryTest {

    private SubAssignmentFactory sut;

    @BeforeEach
    void setUp() {
        sut = new SubAssignmentFactory();
    }

    @Test
    void create_shouldReturnNewSubAssignment() {
        // When
        SubAssignment result = sut.create();

        // Then
        assertThat(result, notNullValue());
    }

}