package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitAPIFactory;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubAPI implements GitAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAPI.class);

    private final GitAPIFactory<GitHub> gitHubAPIFactory;

    public GitHubAPI(final GitAPIFactory<GitHub> gitHubAPIFactory) {
        this.gitHubAPIFactory = gitHubAPIFactory;
    }

    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final String accessToken) throws IOException {
        LOGGER.info("getAllRepositories");

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        List<NotSavedRepositoryInternalDTO> repositories =
            github.getMyself()
                  .getAllRepositories()
                  .values()
                  .stream()
                  .map(repo -> new NotSavedRepositoryInternalDTO(repo.getId(),
                                                                 repo.getName(),
                                                                 repo.getHttpTransportUrl()))
                  .collect(Collectors.toList());

        LOGGER.info("getAllRepositories finished: " + repositories.size());

        return repositories;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken, final long platformId)
        throws IOException {
        LOGGER.info("getRepositoryById: " + platformId);

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        GHRepository repository = github.getRepositoryById(platformId);

        LOGGER.info("getRepositoryById finished: " + repository.getName());

        return NotSavedRepositoryInternalDTO.builder()
                                            .name(repository.getName())
                                            .url(repository.getHttpTransportUrl())
                                            .platformId(repository.getId())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId) throws IOException {
        LOGGER.info("getAllBranches: " + platformId);

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        List<BranchInternalDTO> branches = github.getRepositoryById(platformId)
                                                 .getBranches()
                                                 .values()
                                                 .stream()
                                                 .map(branch -> new BranchInternalDTO(branch.getName()))
                                                 .collect(Collectors.toList());

        LOGGER.info("getAllBranches for repo {} finished", platformId);
        return branches;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final String accessToken, final long id,
                                                 final @Nullable String branchName) throws IOException {
        LOGGER.info("getAllCommits: {}; {}", id, branchName);

        List<CommitInternalDTO> result = new ArrayList<>();

        var github = gitHubAPIFactory.createObject(accessToken);
        String branch = branchName == null ? github.getRepositoryById(id).getDefaultBranch() : branchName;
        github.getRepositoryById(id).queryCommits().from(branch).list().forEach(commit -> {
            LOGGER.info("commit: {}", commit.getSHA1());
            result.add(this.mapGHCommitToInternalDTO(commit));
        });

        LOGGER.info("getAllCommits for repo {} finished", id);

        return result;
    }

    private CommitInternalDTO mapGHCommitToInternalDTO(final GHCommit commit) {

        String author;
        try {
            if (commit.getAuthor() != null) {
                author = commit.getAuthor().getName();
            } else {
                if (commit.getCommitter() != null) {
                    author = commit.getCommitter().getName();
                } else {
                    author = commit.getCommitShortInfo().getAuthor().getName();
                }
            }
        } catch (IOException e) {
            author = "unknown";
        }

        try {
            return CommitInternalDTO.builder()
                                    .id(commit.getSHA1())
                                    .author(author)
                                    .timestamp(commit.getCommitDate())
                                    .message(commit.getCommitShortInfo().getMessage())
                                    .parentIds(commit.getParentSHA1s())
                                    .isMergeCommit(commit.getParents().size() > 1)
                                    .additions(commit.getLinesAdded())
                                    .deletions(commit.getLinesDeleted())
                                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
