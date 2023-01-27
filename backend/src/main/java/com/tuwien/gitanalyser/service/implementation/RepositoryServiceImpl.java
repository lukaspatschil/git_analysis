package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.APICalls.GitAPI;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    private final UserService userService;
    private final GitHubAPI gitHubAPI;
    private final GitLabAPI gitLabAPI;

    public RepositoryServiceImpl(final UserService userService, final GitHubAPI gitHubAPI, final GitLabAPI gitLabAPI) {
        this.userService = userService;
        this.gitHubAPI = gitHubAPI;
        this.gitLabAPI = gitLabAPI;
    }

    @Override
    public List<Repository> getAllRepositories(final Long userId) {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories for user " + userId);

        List<Repository> allRepos;

        try {
            GitAPI gitAPI = getAPI(userId);
            allRepos = gitAPI.getAllRepositories(getAccessToken(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return allRepos;
    }

    @Override
    public Repository getRepositoryById(final Long userId, final Long id) {
        LOGGER.info("RepositoryServiceImpl: getRepositoryById with Id {} for user {}", id, userId);

        Repository repo;

        try {
            GitAPI gitAPI = getAPI(userId);
            repo = gitAPI.getRepositoryById(getAccessToken(userId), id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return repo;
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
