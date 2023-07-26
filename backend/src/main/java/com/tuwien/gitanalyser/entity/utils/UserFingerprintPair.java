package com.tuwien.gitanalyser.entity.utils;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserFingerprintPair {
    private User user;
    private FingerprintPair fingerprintPair;
}
