package com.tuwien.gitanalyser.service.APICalls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import utils.Randoms;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class GitHubAPIFactoryTest {

    GitHubAPIFactory sut;

    @BeforeEach
    void setUp() {
        sut = new GitHubAPIFactory();
    }

    @Test
    void createObject_always_shouldReturnCorrectObject() throws IOException {
        // Given
        String accessToken = Randoms.alpha();

        // When
        GitHub result = sut.createObject(accessToken);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(GitHub.class));
    }
}