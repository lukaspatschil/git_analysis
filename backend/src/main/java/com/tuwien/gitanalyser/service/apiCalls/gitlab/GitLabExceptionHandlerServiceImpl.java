package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import com.tuwien.gitanalyser.service.apiCalls.github.GitHubExceptionHandlerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitLabExceptionHandlerServiceImpl implements GitExceptionHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubExceptionHandlerServiceImpl.class);
    private final GitLabAccessTokenServiceImpl gitLabAccessTokenService;
    private final GitLabRefreshTokenService gitLabRefreshTokenService;

    public GitLabExceptionHandlerServiceImpl(final GitLabAccessTokenServiceImpl gitLabAccessTokenService,
                                             final GitLabRefreshTokenService gitLabRefreshTokenService) {
        this.gitLabAccessTokenService = gitLabAccessTokenService;
        this.gitLabRefreshTokenService = gitLabRefreshTokenService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId)
        throws GitException {
        LOGGER.info("getAllRepositories for user " + userId);

        List<NotSavedRepositoryInternalDTO> allRepos;

        try {
            allRepos = gitLabAccessTokenService.getAllRepositories(userId);
        } catch (TryRefreshException e) {
            gitLabRefreshTokenService.refreshGitAccessToken(userId);
            try {
                allRepos = gitLabAccessTokenService.getAllRepositories(userId);
            } catch (TryRefreshException ex) {
                throw new AuthenticationException(ex.getMessage());
            }
        }

        return allRepos;
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId)
        throws GitException {
        LOGGER.info("getAllBranches for user {} and repository {}", userId, platformId);

        List<BranchInternalDTO> allBranches;
        try {
            allBranches = gitLabAccessTokenService.getAllBranches(userId, platformId);
        } catch (TryRefreshException e) {
            gitLabRefreshTokenService.refreshGitAccessToken(userId);
            try {
                allBranches = gitLabAccessTokenService.getAllBranches(userId, platformId);
            } catch (TryRefreshException ex) {
                throw new AuthenticationException(ex.getMessage());
            }
        }

        LOGGER.info("getAllBranches for user {} and repository {} finished", userId, platformId);
        return allBranches;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {
        LOGGER.info("getRepositoryById with Id {} for user {}", platformId, userId);

        NotSavedRepositoryInternalDTO repository;
        try {
            repository = gitLabAccessTokenService.getRepositoryById(userId, platformId);
        } catch (TryRefreshException e) {
            gitLabRefreshTokenService.refreshGitAccessToken(userId);
            try {
                repository = gitLabAccessTokenService.getRepositoryById(userId, platformId);
            } catch (TryRefreshException ex) {
                throw new AuthenticationException(ex.getMessage());
            }
        }

        LOGGER.info("getRepositoryById with Id {} for user {} finished", platformId, userId);
        return repository;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {
        LOGGER.info("getAllCommits for user {} and repository {} and branch {}", userId, platformId, branch);

        List<CommitInternalDTO> allCommits;
        try {
            allCommits = gitLabAccessTokenService.getAllCommits(userId, platformId, branch);
        } catch (TryRefreshException e) {
            gitLabRefreshTokenService.refreshGitAccessToken(userId);
            try {
                allCommits = gitLabAccessTokenService.getAllCommits(userId, platformId, branch);
            } catch (TryRefreshException ex) {
                throw new AuthenticationException(ex.getMessage());
            }
        }

        LOGGER.info("getAllCommits for user {} and repository {} and branch {} finished with length {}", userId,
                    platformId, branch, allCommits.size());
        return allCommits;
    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId) {
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {}", userId, platformId);

        boolean result;

        try {
            getRepositoryById(userId, platformId);
            result = true;
        } catch (Exception e) {
            result = false;
        }

        return result;
    }
}
