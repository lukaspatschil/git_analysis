package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
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

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(
        final String accessToken) throws GitLabApiException, IOException {
        LOGGER.info("GitLabAPI: getAllRepositories");

        List<NotSavedRepositoryInternalDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = gitLabAPIFactory.createObject(accessToken);

        allRepos.addAll(getOwnedProjects(gitLabApi));
        allRepos.addAll(getMemberProjects(gitLabApi));

        return allRepos;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken, final long platformId)
        throws GitLabApiException, IOException {
        LOGGER.info("GitLabAPI: getRepositoryById: " + platformId);

        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        Project project = gitLabAPI.getProjectApi().getProject(platformId);

        LOGGER.info("GitLabAPI: getRepositoryById: " + project);

        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(project.getId())
                                            .name(project.getName())
                                            .url(project.getHttpUrlToRepo())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId)
        throws IOException, GitLabApiException {
        LOGGER.info("GitLabAPI: getAllBranches for platformId {}", platformId);

        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        List<Branch> branches = gitLabAPI.getRepositoryApi().getBranches(platformId);

        return branches.stream()
                       .map(x -> BranchInternalDTO.builder()
                                                  .name(x.getName())
                                                  .build())
                       .collect(Collectors.toList());
    }

    private List<NotSavedRepositoryInternalDTO> convertToRepository(final List<Project> projects) {
        return projects.stream()
                       .map(project ->
                                NotSavedRepositoryInternalDTO.builder()
                                                             .name(project.getName())
                                                             .url(project.getHttpUrlToRepo())
                                                             .platformId(project.getId())
                                                             .build())
                       .collect(Collectors.toList());
    }

    private List<NotSavedRepositoryInternalDTO> getOwnedProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepository(gitLabApi.getProjectApi().getOwnedProjects());
    }

    private List<NotSavedRepositoryInternalDTO> getMemberProjects(final GitLabApi gitLabApi) throws GitLabApiException {
        return convertToRepository(gitLabApi.getProjectApi().getMemberProjects());
    }
}

