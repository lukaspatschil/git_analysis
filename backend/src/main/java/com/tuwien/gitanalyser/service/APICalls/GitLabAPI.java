package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.security.GitLabOAuthProviderProperties;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GitLabAPI implements GitAPI {

    static Logger LOGGER = LoggerFactory.getLogger(GitLabAPI.class);

    public static List<RepositoryDTO> getAllRepositories(String tokenValue) throws GitLabApiException {
        LOGGER.info("GitLabAPI: getAllRepositories");

        List<RepositoryDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = new GitLabApi(GitLabOAuthProviderProperties.CLIENT_URL,
            Constants.TokenType.OAUTH2_ACCESS, tokenValue);

        gitLabApi.getProjectApi().getOwnedProjects().forEach(project -> {
            LOGGER.info("GitLabAPI: getAllRepositories: " + project.getName());
            allRepos.add(new RepositoryDTO(project.getName()));
        });

        gitLabApi.getProjectApi().getMemberProjects().forEach(project -> {
            LOGGER.info("GitLabAPI: getMemberRepositories: " + project.getName());
            allRepos.add(new RepositoryDTO(project.getName()));
        });

        return allRepos;
    }
}

