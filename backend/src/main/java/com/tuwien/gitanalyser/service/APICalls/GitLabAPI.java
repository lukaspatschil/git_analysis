package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitAPIFactory;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
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
        LOGGER.info("getAllRepositories");

        List<NotSavedRepositoryInternalDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi = gitLabAPIFactory.createObject(accessToken);

        allRepos.addAll(getOwnedProjects(gitLabApi));
        allRepos.addAll(getMemberProjects(gitLabApi));

        LOGGER.info("getAllRepositories finished: " + allRepos.size());

        return allRepos;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken, final long platformId)
        throws GitLabApiException, IOException {
        LOGGER.info("getRepositoryById: " + platformId);

        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        Project project = gitLabAPI.getProjectApi().getProject(platformId);

        LOGGER.info("getRepositoryById: " + project);

        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(project.getId())
                                            .name(project.getName())
                                            .url(project.getHttpUrlToRepo())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId)
        throws IOException, GitLabApiException {
        LOGGER.info("getAllBranches for platformId {}", platformId);

        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        List<Branch> branches = gitLabAPI.getRepositoryApi().getBranches(platformId);

        return branches.stream()
                       .map(x -> BranchInternalDTO.builder()
                                                  .name(x.getName())
                                                  .build())
                       .collect(Collectors.toList());
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final String accessToken, final long id, final String branchName)
        throws IOException, GitLabApiException {
        LOGGER.info("getAllCommits for platformId {} and branch {}", id, branchName);

        List<CommitInternalDTO> result;

        GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
        List<Commit> commits = gitLabAPI.getCommitsApi().getCommits(id, branchName, null, null, null, true, true,
                                                                    null);
        result = commits.stream()
                        .map(this::mapCommitsToInternalDTO)
                        .collect(Collectors.toList());

        LOGGER.info("getAllCommits finished for platformId {} and branch {}", id, branchName);

        return result;
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

    private CommitInternalDTO mapCommitsToInternalDTO(final Commit commit) {
        return CommitInternalDTO.builder()
                                .id(commit.getId())
                                .author(commit.getAuthorName())
                                .timestamp(commit.getCommittedDate())
                                .message(commit.getMessage())
                                .parentIds(commit.getParentIds())
                                .isMergeCommit(commit.getParentIds().size() > 1)
                                .additions(commit.getStats().getAdditions())
                                .deletions(commit.getStats().getDeletions())
                                .build();
    }
}

