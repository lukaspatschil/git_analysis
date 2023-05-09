package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.service.GitAccessTokenService;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubAccessTokenServiceImpl implements GitAccessTokenService {

    private final GitHubAPI gitHubAPI;
    private final UserService userService;

    public GitHubAccessTokenServiceImpl(final GitHubAPI gitHubAPI, final UserService userService) {
        this.gitHubAPI = gitHubAPI;
        this.userService = userService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) throws GitException {
        return gitHubAPI.getAllRepositories(getAccessToken(userId));
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId) throws GitException {
        return gitHubAPI.getAllBranches(getAccessToken(userId), platformId);
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException {
        return gitHubAPI.getRepositoryById(getAccessToken(userId), platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException {
        return gitHubAPI.getAllCommits(getAccessToken(userId), platformId, branch);
    }

    private String getAccessToken(final Long userId) {
        return userService.getUser(userId).getAccessToken();
    }
}
