package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubExceptionHandlerServiceImpl implements GitExceptionHandlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubExceptionHandlerServiceImpl.class);
    private final GitHubAccessTokenServiceImpl gitHubAccessTokenService;

    public GitHubExceptionHandlerServiceImpl(final GitHubAccessTokenServiceImpl gitHubAccessTokenService) {
        this.gitHubAccessTokenService = gitHubAccessTokenService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) throws GitException {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories for user " + userId);
        return gitHubAccessTokenService.getAllRepositories(userId);
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId) throws GitException {
        LOGGER.info("getAllBranches for user {} and repository {}", userId, platformId);
        return gitHubAccessTokenService.getAllBranches(userId, platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {
        LOGGER.info("getRepositoryById with Id {} for user {}", platformId, userId);
        return gitHubAccessTokenService.getRepositoryById(userId, platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {
        LOGGER.info("getAllCommits for user {} and repository {} and branch {}", userId, platformId, branch);
        return gitHubAccessTokenService.getAllCommits(userId, platformId, branch);
    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId) {
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {}", userId, platformId);
        boolean result;
        try {
            getRepositoryById(userId, platformId);
            result = true;
        } catch (Exception e) {
            LOGGER.error("repositoryAccessibleByUser for user {} and repository {} failed: {}", userId, platformId, e);
            result = false;
        }
        return result;
    }

}
