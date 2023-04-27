package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
import com.tuwien.gitanalyser.service.GitAccessTokenService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitLabAccessTokenServiceImpl implements GitAccessTokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitLabAccessTokenServiceImpl.class);
    private final GitLabAPI gitLabAPI;
    private final UserService userService;

    public GitLabAccessTokenServiceImpl(final GitLabAPI gitLabAPI, final UserService userService) {
        this.gitLabAPI = gitLabAPI;
        this.userService = userService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId)
        throws GitLabException, TryRefreshException {
        LOGGER.info("getAllRepositories for user {}", userId);
        return gitLabAPI.getAllRepositories(getAccessToken(userId));
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId)
        throws GitLabException, TryRefreshException {
        LOGGER.info("getAllBranches for user {} and platformId {}", userId, platformId);
        return gitLabAPI.getAllBranches(getAccessToken(userId), platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitLabException, TryRefreshException {
        LOGGER.info("getRepositoryById for user {} and platform {}", userId, platformId);
        return gitLabAPI.getRepositoryById(getAccessToken(userId), platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitLabException, TryRefreshException {
        LOGGER.info("getAllCommits for user {} and platform {} and branch {}", userId, platformId, branch);
        return gitLabAPI.getAllCommits(getAccessToken(userId), platformId, branch);
    }

    private String getAccessToken(final Long userId) {
        return userService.getUser(userId).getAccessToken();
    }
}
