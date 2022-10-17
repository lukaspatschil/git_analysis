package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.security.GitLabOAuthProviderProperties;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitLabAPI implements GitAPI {

    static Logger LOGGER = LoggerFactory.getLogger(GitLabAPI.class);

    public List<RepositoryDTO> getAllRepositories(String tokenValue) throws GitLabApiException {
        LOGGER.info("GitLabAPI: getAllRepositories");

        List<RepositoryDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = new GitLabApi(GitLabOAuthProviderProperties.CLIENT_URL, Constants.TokenType.OAUTH2_ACCESS,
                                            tokenValue);

        allRepos.addAll(getOwnedProjects(gitLabApi));
        allRepos.addAll(getMemberProjects(gitLabApi));

        return allRepos;
    }

    private static List<RepositoryDTO> getOwnedProjects(GitLabApi gitLabApi) throws GitLabApiException {
        return gitLabApi.getProjectApi().getOwnedProjects().stream()
                        .map(project -> new RepositoryDTO(project.getId(), project.getName()))
                        .collect(Collectors.toList());
    }

    private static List<RepositoryDTO> getMemberProjects(GitLabApi gitLabApi) throws GitLabApiException {
        return gitLabApi.getProjectApi().getMemberProjects().stream()
                        .map(project -> new RepositoryDTO(project.getId(), project.getName()))
                        .collect(Collectors.toList());
    }
}

