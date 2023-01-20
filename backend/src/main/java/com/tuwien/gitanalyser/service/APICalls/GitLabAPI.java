package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitLabAPI implements GitAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitLabAPI.class);

    private final GitLabAPIFactory gitLabAPIFactory;

    public GitLabAPI(final GitLabAPIFactory gitLabAPIFactory) {
        this.gitLabAPIFactory = gitLabAPIFactory;
    }

    public List<RepositoryDTO> getAllRepositories(final String accessToken) throws GitLabApiException {
        LOGGER.info("GitLabAPI: getAllRepositories");

        List<RepositoryDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = gitLabAPIFactory.createObject(accessToken);

        allRepos.addAll(getOwnedProjects(gitLabApi));
        allRepos.addAll(getMemberProjects(gitLabApi));

        return allRepos;
    }

    @Override
    public RepositoryDTO getRepositoryById(final String accessToken, final long id) throws GitLabApiException {
        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        Project project = gitLabAPI.getProjectApi().getProject(id);

        return new RepositoryDTO(project.getId(), project.getName(), project.getHttpUrlToRepo());
    }

    private static List<RepositoryDTO> getOwnedProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepositoryDTO(gitLabApi.getProjectApi().getOwnedProjects());
    }

    private static List<RepositoryDTO> getMemberProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepositoryDTO(gitLabApi.getProjectApi().getMemberProjects());
    }

    private static List<RepositoryDTO> convertToRepositoryDTO(final List<Project> projects) {
        return projects.stream()
                       .map(project ->
                                new RepositoryDTO(project.getId(), project.getName(), project.getHttpUrlToRepo()))
                       .collect(Collectors.toList());
    }
}

