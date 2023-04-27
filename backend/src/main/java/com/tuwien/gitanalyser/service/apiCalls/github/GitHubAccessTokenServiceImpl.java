package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.service.GitAccessTokenService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubAccessTokenServiceImpl implements GitAccessTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAccessTokenServiceImpl.class);
    private final GitHubAPI gitHubAPI;
    private final UserService userService;

    public GitHubAccessTokenServiceImpl(final GitHubAPI gitHubAPI, final UserService userService) {
        this.gitHubAPI = gitHubAPI;
        this.userService = userService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) throws GitException {
        LOGGER.info("getAllRepositories for userId {}", userId);
        return gitHubAPI.getAllRepositories(getAccessToken(userId));
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId) throws GitException {
        LOGGER.info("getAllBranches for user {} and plattformId {}", userId, platformId);
        return gitHubAPI.getAllBranches(getAccessToken(userId), platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {
        LOGGER.info("getRepositoryById for user {} and plattformId {}", userId, platformId);
        return gitHubAPI.getRepositoryById(getAccessToken(userId), platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {
        LOGGER.info("getAllCommits for user {} and plattformId {} and branch {}", userId, platformId, branch);
        return gitHubAPI.getAllCommits(getAccessToken(userId), platformId, branch);
    }

    private String getAccessToken(final Long userId) {
        return userService.getUser(userId).getAccessToken();
    }
}
