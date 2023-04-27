package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.exception.GitLabException;

public interface GitRefreshTokenService {
    void refreshGitAccessToken(Long userId) throws GitLabException;
}
