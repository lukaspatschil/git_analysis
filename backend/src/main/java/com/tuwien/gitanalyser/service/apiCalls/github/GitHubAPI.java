package com.tuwien.gitanalyser.service.apiCalls.github;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.GitHubException;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitAPIFactory;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHEmail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubAPI implements GitAPI {

    private final GitAPIFactory<GitHub> gitHubAPIFactory;

    public GitHubAPI(final GitAPIFactory<GitHub> gitHubAPIFactory) {
        this.gitHubAPIFactory = gitHubAPIFactory;
    }

    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final String accessToken) throws GitHubException {

        List<NotSavedRepositoryInternalDTO> repositories;

        try {
            GitHub github = gitHubAPIFactory.createObject(accessToken);
            repositories = github.getMyself()
                                 .getAllRepositories()
                                 .values()
                                 .stream()
                                 .map(repo -> new NotSavedRepositoryInternalDTO(repo.getId(),
                                                                                repo.getName(),
                                                                                repo.getHttpTransportUrl()))
                                 .collect(Collectors.toList());
        } catch (IOException e) {
            throw new GitHubException(e);
        }

        return repositories;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken, final long platformId)
        throws GitHubException {

        GHRepository repository;
        try {
            GitHub github = gitHubAPIFactory.createObject(accessToken);
            repository = github.getRepositoryById(platformId);
        } catch (IOException e) {
            throw new GitHubException(e);
        }

        return NotSavedRepositoryInternalDTO.builder()
                                            .name(repository.getName())
                                            .url(repository.getHttpTransportUrl())
                                            .platformId(repository.getId())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId)
        throws GitHubException {

        List<BranchInternalDTO> branches;
        try {
            GitHub github = gitHubAPIFactory.createObject(accessToken);
            branches = github.getRepositoryById(platformId)
                             .getBranches()
                             .values()
                             .stream()
                             .map(branch -> new BranchInternalDTO(branch.getName()))
                             .collect(Collectors.toList());
        } catch (IOException e) {
            throw new GitHubException(e);
        }
        return branches;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final String accessToken, final long platformId,
                                                 final @Nullable String branchName) throws GitHubException {

        List<CommitInternalDTO> result = new ArrayList<>();

        try {
            GitHub github = gitHubAPIFactory.createObject(accessToken);
            String branch = branchName == null ? github.getRepositoryById(platformId).getDefaultBranch() : branchName;
            github.getRepositoryById(platformId)
                  .queryCommits()
                  .from(branch)
                  .list()
                  .forEach(commit -> result.add(this.mapGHCommitToInternalDTO(commit)));
        } catch (IOException e) {
            throw new GitHubException(e);
        }

        return result.stream().sorted(Comparator.comparing(CommitInternalDTO::getTimestamp)).toList();
    }

    public String getEmail(final String accessToken) throws GitException {
        try {
            GitHub github = gitHubAPIFactory.createObject(accessToken);
            List<GHEmail> emails2 = github.getMyself().getEmails2();

            // filter primary email
            return emails2.stream().filter(GHEmail::isPrimary).toList().get(0).getEmail();
        } catch (Exception e) {
            throw new GitHubException(e);
        }
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
