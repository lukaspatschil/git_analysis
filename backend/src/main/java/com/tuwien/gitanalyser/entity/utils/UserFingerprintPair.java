package com.tuwien.gitanalyser.entity.utils;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFingerprintPair {
    private User user;
    private FingerprintPair fingerprintPair;
}
