package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
import com.tuwien.gitanalyser.service.GitAccessTokenService;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitLabAccessTokenServiceImpl implements GitAccessTokenService {
    private final GitLabAPI gitLabAPI;
    private final UserService userService;

    public GitLabAccessTokenServiceImpl(final GitLabAPI gitLabAPI, final UserService userService) {
        this.gitLabAPI = gitLabAPI;
        this.userService = userService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId)
        throws GitLabException, TryRefreshException {
        return gitLabAPI.getAllRepositories(getAccessToken(userId));
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId)
        throws GitLabException, TryRefreshException {
        return gitLabAPI.getAllBranches(getAccessToken(userId), platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitLabException, TryRefreshException {
        return gitLabAPI.getRepositoryById(getAccessToken(userId), platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitLabException, TryRefreshException {
        return gitLabAPI.getAllCommits(getAccessToken(userId), platformId, branch);
    }

    private String getAccessToken(final Long userId) {
        return userService.getUser(userId).getAccessToken();
    }
}
