package com.tuwien.gitanalyser.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FingerprintPair {
    private String fingerprint;
    private String hash;
}
