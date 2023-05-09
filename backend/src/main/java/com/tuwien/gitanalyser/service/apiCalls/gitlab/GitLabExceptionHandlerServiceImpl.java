package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitLabExceptionHandlerServiceImpl implements GitExceptionHandlerService {

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

        return allBranches;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {

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

        return repository;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {

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

        return allCommits;
    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId) {

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
