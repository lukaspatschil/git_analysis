package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.exception.TryRefreshException;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitAPIFactory;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final String accessToken)
        throws GitLabException, TryRefreshException {

        List<NotSavedRepositoryInternalDTO> allRepos = new ArrayList<>();

        GitLabApi gitLabApi;
        try {
            gitLabApi = gitLabAPIFactory.createObject(accessToken);
            allRepos.addAll(getOwnedProjects(gitLabApi));
            allRepos.addAll(getMemberProjects(gitLabApi));

        } catch (IOException e) {
            throw new GitLabException(e);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED.value()) {
                throw new TryRefreshException(e);
            }
            throw new GitLabException(e);
        }

        return allRepos;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken, final long platformId)
        throws GitLabException, TryRefreshException {

        Project project;
        try {
            GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
            project = gitLabAPI.getProjectApi().getProject(platformId);
        } catch (IOException e) {
            throw new GitLabException(e);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED.value()) {
                throw new TryRefreshException(e);
            }
            throw new GitLabException(e);
        }


        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(project.getId())
                                            .name(project.getName())
                                            .url(project.getHttpUrlToRepo())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId)
        throws GitLabException, TryRefreshException {

        List<Branch> branches;
        try {
            GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
            branches = gitLabAPI.getRepositoryApi().getBranches(platformId);
        } catch (IOException e) {
            throw new GitLabException(e);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED.value()) {
                throw new TryRefreshException(e);
            }
            throw new GitLabException(e);
        }

        return branches.stream()
                       .map(x -> BranchInternalDTO.builder()
                                                  .name(x.getName())
                                                  .build())
                       .collect(Collectors.toList());
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final String accessToken, final long platformId,
                                                 final String branchName) throws GitLabException, TryRefreshException {

        List<CommitInternalDTO> result;

        List<Commit> commits;
        try {
            GitLabApi gitLabAPI = gitLabAPIFactory.createObject(accessToken);
            commits = gitLabAPI.getCommitsApi()
                               .getCommits(platformId, branchName, null, null, null, true, true,
                                           null);
        } catch (IOException e) {
            throw new GitLabException(e);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED.value()) {
                throw new TryRefreshException(e);
            }
            throw new GitLabException(e);
        }

        result = commits.stream()
                        .map(this::mapCommitsToInternalDTO)
                        .collect(Collectors.toList());

        return result;
    }

    private List<NotSavedRepositoryInternalDTO> convertToRepositories(final List<Project> projects) {
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
        return convertToRepositories(gitLabApi.getProjectApi().getOwnedProjects());
    }

    private List<NotSavedRepositoryInternalDTO> getMemberProjects(final GitLabApi gitLabApi)
        throws GitLabApiException {
        return convertToRepositories(gitLabApi.getProjectApi().getMemberProjects());
    }

    private CommitInternalDTO mapCommitsToInternalDTO(final Commit commit) {
        CommitInternalDTO mappedCommit = CommitInternalDTO.builder()
                                                   .id(commit.getId())
                                                   .author(commit.getAuthorName())
                                                   .timestamp(commit.getCommittedDate())
                                                   .message(commit.getMessage())
                                                   .parentIds(commit.getParentIds())
                                                   .isMergeCommit(commit.getParentIds().size() > 1)
                                                   .additions(commit.getStats().getAdditions())
                                                   .deletions(commit.getStats().getDeletions())
                                                   .build();
        LOGGER.info("Commit: {}; Mapped commit: {}", commit, mappedCommit);
        return mappedCommit;
    }
}

