package com.tuwien.gitanalyser.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class RepositoryFactoryTest {

    private RepositoryFactory repositoryFactory;

    @BeforeEach
    void setUp() {
        repositoryFactory = new RepositoryFactory();
    }

    @Test
    void create() {
        // Given

        // When
        Repository repository = repositoryFactory.create();

        // Then
        assertThat(repository, notNullValue());

    }

}