package com.tuwien.gitanalyser.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class FingerprintServiceTest {

    FingerprintService sut;

    @BeforeEach
    void setUp() {
        sut = new FingerprintService();
    }

    @Test
    void createFingerprint() {
        // Given

        // When
        FingerprintPair result = sut.createFingerprint();

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getFingerprint(), notNullValue());
        assertThat(result.getHash(), notNullValue());
    }
}