package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.UserService;
import com.tuwien.gitanalyser.service.apiCalls.GitHubAPI;
import com.tuwien.gitanalyser.service.apiCalls.GitLabAPI;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GitServiceImpl implements GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServiceImpl.class);
    private final UserService userService;
    private final GitHubAPI gitHubAPI;
    private final GitLabAPI gitLabAPI;

    public GitServiceImpl(final UserService userService,
                          final GitHubAPI gitHubAPI,
                          final GitLabAPI gitLabAPI) {
        this.userService = userService;
        this.gitHubAPI = gitHubAPI;
        this.gitLabAPI = gitLabAPI;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories for user " + userId);

        List<NotSavedRepositoryInternalDTO> allRepos;

        try {
            GitAPI gitAPI = getAPI(userId);
            allRepos = gitAPI.getAllRepositories(getAccessToken(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return allRepos;
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId)
        throws GitLabApiException, IOException {
        LOGGER.info("getAllBranches for user {} and repository {}", userId, platformId);

        GitAPI gitAPI = getAPI(userId);
        List<BranchInternalDTO> allBranches = gitAPI.getAllBranches(getAccessToken(userId), platformId);

        LOGGER.info("getAllBranches for user {} and repository {} finished", userId, platformId);
        return allBranches;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitLabApiException, IOException {
        LOGGER.info("getRepositoryById with Id {} for user {}", platformId, userId);

        GitAPI gitAPI = getAPI(userId);
        NotSavedRepositoryInternalDTO nSRIDTO = gitAPI.getRepositoryById(getAccessToken(userId), platformId);

        LOGGER.info("getRepositoryById with Id {} for user {} finished", platformId, userId);
        return nSRIDTO;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitLabApiException, IOException {
        LOGGER.info("getAllCommits for user {} and repository {} and branch {}", userId, platformId, branch);

        GitAPI gitApi = getAPI(userId);
        List<CommitInternalDTO> allCommits = gitApi.getAllCommits(getAccessToken(userId), platformId, branch);

        LOGGER.info("getAllCommits for user {} and repository {} and branch {} finished with length {}", userId,
                    platformId, branch, allCommits.size());
        return allCommits;
    }

    @Override
    public Set<CommitterInternalDTO> getAllCommitters(final long userId, final Long platformId, final String branch)
        throws GitLabApiException, IOException {
        LOGGER.info("getAllCommitters for user {} and repository {} and branch {}", userId, platformId, branch);

        Set<CommitterInternalDTO> result = new HashSet<>();

        GitAPI gitApi = getAPI(userId);
        List<CommitInternalDTO> allCommits = gitApi.getAllCommits(getAccessToken(userId), platformId, branch);

        for (CommitInternalDTO commit : allCommits) {
            result.add(new CommitterInternalDTO(commit.getAuthor()));
        }

        LOGGER.info("getAllCommitters for user {} and repository {} and branch {} finished with length {}", userId,
                    platformId, branch, result.size());

        return result;

    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId) {
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {}", userId, platformId);

        boolean result;

        GitAPI gitApi = getAPI(userId);
        try {
            gitApi.getRepositoryById(getAccessToken(userId), platformId);
            result = true;
        } catch (Exception e) {
            LOGGER.error("repositoryAccessibleByUser for user {} and repository {} failed: {}", userId, platformId, e);
            result = false;
        }
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {} finished positive", userId, platformId);
        return result;

    }

    private GitAPI getAPI(final Long userId) throws NotFoundException {
        return switch (getUser(userId).getAuthenticationProvider().name().toLowerCase()) {
            case AuthenticationConstants.GITHUB_REGISTRATION_ID -> gitHubAPI;
            case AuthenticationConstants.GITLAB_REGISTRATION_ID -> gitLabAPI;
            default -> throw new RuntimeException("No API for this client");
        };
    }

    private String getAccessToken(final Long userId) throws NotFoundException {
        return userService.getUser(userId).getAccessToken();
    }

    private User getUser(final Long userId) throws NotFoundException {
        return userService.getUser(userId);
    }
}
