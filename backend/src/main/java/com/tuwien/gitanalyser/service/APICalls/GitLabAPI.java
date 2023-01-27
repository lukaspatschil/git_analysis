package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.entity.Repository;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitLabAPI implements GitAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitLabAPI.class);

    private final GitAPIFactory<GitLabApi> gitLabAPIFactory;

    public GitLabAPI(final GitAPIFactory<GitLabApi> gitLabAPIFactory) {
        this.gitLabAPIFactory = gitLabAPIFactory;
    }

    private static List<Repository> getOwnedProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepository(gitLabApi.getProjectApi().getOwnedProjects());
    }

    private static List<Repository> getMemberProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepository(gitLabApi.getProjectApi().getMemberProjects());
    }

    private static List<Repository> convertToRepository(final List<Project> projects) {
        return projects.stream()
                       .map(project ->
                                new Repository(project.getId(), project.getName(), project.getHttpUrlToRepo()))
                       .collect(Collectors.toList());
    }

    public List<Repository> getAllRepositories(final String accessToken) throws GitLabApiException, IOException {
        LOGGER.info("GitLabAPI: getAllRepositories");

        List<Repository> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = gitLabAPIFactory.createObject(accessToken);

        allRepos.addAll(getOwnedProjects(gitLabApi));
        allRepos.addAll(getMemberProjects(gitLabApi));

        return allRepos;
    }

    @Override
    public Repository getRepositoryById(final String accessToken, final long id)
        throws GitLabApiException, IOException {
        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        Project project = gitLabAPI.getProjectApi().getProject(id);

        return new Repository(project.getId(), project.getName(), project.getHttpUrlToRepo());
    }
}

