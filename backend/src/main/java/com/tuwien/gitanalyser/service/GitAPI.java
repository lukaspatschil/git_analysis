package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface GitAPI {
    /**
     * returns all repositories from the git api.
     */
    List<NotSavedRepositoryInternalDTO> getAllRepositories(String tokenValue) throws IOException, GitLabApiException;

    /**
     * returns a repository by its id.
     */
    NotSavedRepositoryInternalDTO getRepositoryById(String accessToken, long platformId)
        throws GitLabApiException, IOException;

    /**
     * returns all branches from the git api.
     */
    List<BranchInternalDTO> getAllBranches(String accessToken, Long platformId) throws IOException, GitLabApiException;

    /**
     * returns all commits from the git api. If branchName is null, the commits from the default branch are returned.
     */
    List<CommitInternalDTO> getAllCommits(String accessToken, long platformId, String branchName)
        throws IOException, GitLabApiException;
}
