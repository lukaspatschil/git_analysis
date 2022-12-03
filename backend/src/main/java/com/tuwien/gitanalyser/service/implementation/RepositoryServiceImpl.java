package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.APICalls.GitAPI;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    private final GitHubAPI gitHubAPI;
    private final GitLabAPI gitLabAPI;

    public RepositoryServiceImpl(final GitHubAPI gitHubAPI, final GitLabAPI gitLabAPI) {
        this.gitHubAPI = gitHubAPI;
        this.gitLabAPI = gitLabAPI;
    }

    @Override
    public List<RepositoryDTO> getAllRepositories(final OAuth2AuthorizedClient client) {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories");

        List<RepositoryDTO> allRepos;

        try {
            GitAPI gitAPI = getAPI(client);
            allRepos = gitAPI.getAllRepositories(client.getAccessToken().getTokenValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return allRepos;
    }

    @Override
    public RepositoryDTO getRepositoryById(final OAuth2AuthorizedClient client, final long id) {
        LOGGER.info("RepositoryServiceImpl: getRepositoryById " + id);

        RepositoryDTO repo;

        try {
            GitAPI gitAPI = getAPI(client);
            repo = gitAPI.getRepositoryById(client.getAccessToken().getTokenValue(), id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return repo;
    }

    private GitAPI getAPI(final OAuth2AuthorizedClient client) {
        return switch (client.getClientRegistration().getClientName()) {
            case AuthenticationConstants.GITHUB_CLIENT_NAME -> gitHubAPI;
            case AuthenticationConstants.GITLAB_CLIENT_NAME -> gitLabAPI;
            default -> throw new RuntimeException("No API for this client");
        };
    }
}
