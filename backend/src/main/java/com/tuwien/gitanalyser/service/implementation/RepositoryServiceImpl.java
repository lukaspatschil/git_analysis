package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.security.GitHubOAuthProviderProperties;
import com.tuwien.gitanalyser.security.GitLabOAuthProviderProperties;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    @Override
    public List<RepositoryDTO> getAllRepositories(OAuth2AuthorizedClient client) {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories");

        List<RepositoryDTO> allRepos = new ArrayList<>();

        try {
            switch (client.getClientRegistration().getClientName()) {
                case GitHubOAuthProviderProperties.CLIENT_NAME:
                    allRepos = GitHubAPI.getAllRepositories(client.getAccessToken().getTokenValue());
                    break;
                case GitLabOAuthProviderProperties.CLIENT_NAME:
                    allRepos = GitLabAPI.getAllRepositories(client.getAccessToken().getTokenValue());
                    break;
            }
        } catch (IOException | GitLabApiException e) {
            throw new RuntimeException(e);
        }

        return allRepos;
    }
}
