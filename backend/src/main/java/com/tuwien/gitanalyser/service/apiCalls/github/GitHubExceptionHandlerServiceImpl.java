package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubExceptionHandlerServiceImpl implements GitExceptionHandlerService {
    private final GitHubAccessTokenServiceImpl gitHubAccessTokenService;

    public GitHubExceptionHandlerServiceImpl(final GitHubAccessTokenServiceImpl gitHubAccessTokenService) {
        this.gitHubAccessTokenService = gitHubAccessTokenService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) throws GitException {
        return gitHubAccessTokenService.getAllRepositories(userId);
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId) throws GitException {
        return gitHubAccessTokenService.getAllBranches(userId, platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {
        return gitHubAccessTokenService.getRepositoryById(userId, platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {
        return gitHubAccessTokenService.getAllCommits(userId, platformId, branch);
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
