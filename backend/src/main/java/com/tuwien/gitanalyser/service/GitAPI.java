package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.TryRefreshException;

import java.util.List;

public interface GitAPI {
    /**
     * returns all repositories from the git api.
     */
    List<NotSavedRepositoryInternalDTO> getAllRepositories(String tokenValue) throws GitException, TryRefreshException;

    /**
     * returns a repository by its id.
     */
    NotSavedRepositoryInternalDTO getRepositoryById(String accessToken, long platformId)
        throws GitException, TryRefreshException;

    /**
     * returns all branches from the git api.
     */
    List<BranchInternalDTO> getAllBranches(String accessToken, Long platformId)
        throws GitException, TryRefreshException;

    /**
     * returns all commits from the git api. If branchName is null, the commits from the default branch are returned.
     */
    List<CommitInternalDTO> getAllCommits(String accessToken, long platformId, String branchName)
        throws GitException, TryRefreshException;
}
