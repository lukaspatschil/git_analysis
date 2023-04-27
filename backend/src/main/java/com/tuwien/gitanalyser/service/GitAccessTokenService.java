package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.TryRefreshException;

import java.util.List;

/**
 * responsible to call the git api with the correct access token.
 */
public interface GitAccessTokenService {

    /**
     * loads all repositories.
     * @param userId of the current user
     * @return List of repositories
     * @throws GitException if something went wrong
     * @throws TryRefreshException if the access token needs to be updated
     */
    List<NotSavedRepositoryInternalDTO> getAllRepositories(Long userId) throws GitException, TryRefreshException;

    /**
     * loads all branches.
     * @param userId of the current user
     * @param platformId of the repository
     * @return List of branches
     * @throws GitException if something went wrong
     * @throws TryRefreshException if the access token needs to be updated
     */
    List<BranchInternalDTO> getAllBranches(Long userId, Long platformId)
        throws GitException, TryRefreshException;

    /**
     * loads a repository with a certain id.
     * @param userId of the current user
     * @param platformId of the repository
     * @return single repository
     * @throws GitException if something went wrong
     * @throws TryRefreshException if the access token needs to be updated
     */
    NotSavedRepositoryInternalDTO getRepositoryById(Long userId, Long platformId)
        throws GitException, TryRefreshException;

    /**
     * loads all commits.
     * @param userId of the current user
     * @param platformId of the repository
     * @param branch branch name
     * @return List of commits
     * @throws GitException if something went wrong
     * @throws TryRefreshException if the access token needs to be updated
     */
    List<CommitInternalDTO> getAllCommits(long userId, Long platformId, String branch)
        throws GitException, TryRefreshException;
}
