package com.tuwien.gitanalyser.service.apiCalls;

import com.tuwien.gitanalyser.service.apiCalls.factory.GitLabAPIFactory;
import org.gitlab4j.api.GitLabApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class GitLabAPIFactoryTest {

    GitLabAPIFactory sut;

    @BeforeEach
    void setUp() {
        sut = new GitLabAPIFactory();
    }

    @Test
    void createObject_always_shouldReturnCorrectObject() {
        // Given
        String accessToken = Randoms.alpha();

        // When
        GitLabApi result = sut.createObject(accessToken);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(GitLabApi.class));
    }

}